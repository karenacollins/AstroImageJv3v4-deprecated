import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.plugin.FITSDecoder;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import skyview.data.CoordinateFormatter;
import skyview.geometry.WCS;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nom.tam.fits.header.InstrumentDescription.FILTER;
import static nom.tam.fits.header.ObservationDescription.DEC;
import static nom.tam.fits.header.ObservationDescription.RA;
import static nom.tam.fits.header.ObservationDurationDescription.EXPTIME;
import static nom.tam.fits.header.Standard.NAXIS;

@SuppressWarnings("unused")
public class NTFITS_Reader extends ImagePlus implements PlugIn
{
    private WCS wcs;
    private FileInfo fileInfo;
    private ImagePlus imagePlus;
    private FITSDecoder fitsDecoder;
    private String fileName;
    private Fits fits;
    private int wi;
    private int he;
    private int de;
    private String imageDescription;

    /**
     * Main processing method for the FITS_Reader object
     *
     * @param path path of FITS file
     */
    public void run(String path)
    {
        wcs = null;
        imagePlus = null;

        /*
         * Extract array of HDU from FITS file using nom.tam.fits
         * This also uses the old style FITS decoder to create a FileInfo.
         */
        BasicHDU[] hdus;
        try
        {
            hdus = getHDU(path);
        }
        catch (FitsException e)
        {
            IJ.error("Unable to open FITS file " + path + ": " + e.getMessage());
            return;
        }

        /*
         * For fpacked files the image is in the second HDU. For uncompressed images
         * it is the first HDU.
         */
        BasicHDU displayHdu;
        if (isCompressedFormat(hdus))
        {
            int imageIndex = 1;
            try
            {
                displayHdu = getCompressedImageData((CompressedImageHDU) hdus[imageIndex]);
            }
            catch (FitsException e)
            {
                IJ.error("Failed to uncompress image: " + e.getMessage());
                return;
            }
        }
        else
        {
            int imageIndex = 0;
            displayHdu = hdus[imageIndex];
            try
            {
                fixDimensions(displayHdu, displayHdu.getAxes().length);
            }
            catch (FitsException e)
            {
                IJ.error("Failed to set image dimensions: "+ e.getMessage());
            }
        }

        Data imgData = getImageData(displayHdu);

        buildImageDescriptionFromHeader(displayHdu);

        if ((wi < 0) || (he < 0))
        {
            IJ.error("This does not appear to be a FITS file. " + wi + " " + he);
            return;
        }
        if (de == 1)
        {
            try
            {
                displaySingleImage(displayHdu, imgData);
            }
            catch (FitsException e)
            {
                IJ.error("Failed to display single image: " + e.getMessage());
                return;
            }
        }
        else
        {
            displayStackedImage();
        }

        if (fitsDecoder != null)
        {
            setProperty("Info", imageDescription + fitsDecoder.getHeaderInfo());
        }
        if (fileInfo != null)
        {
            setFileInfo(fileInfo);
        }
        show();

        IJ.showStatus("");
        try
        {
            writeTemporaryFITSFile(displayHdu);
        }
        catch (FileNotFoundException e)
        {
            IJ.error("File not found: " + e.getMessage() );
        }
        catch (FitsException e)
        {
            IJ.error("This does not appear to be a FITS file: " + e.getMessage());
        }
    }

    private boolean isCompressedFormat(BasicHDU[] basicHDU)
    {
        return basicHDU[0].getHeader().getIntValue(NAXIS) == 0;
    }

    private ImageHDU getCompressedImageData(CompressedImageHDU hdu) throws FitsException
    {
        wi = hdu.getHeader().getIntValue("ZNAXIS1");
        he = hdu.getHeader().getIntValue("ZNAXIS2");
        de = 1;
        return hdu.asImageHDU();
    }

    private void displayStackedImage()
    {
        ImageStack stack = imagePlus.getStack();
        for (int i = 1; i <= stack.getSize(); i++)
        {
            stack.getProcessor(i).flipVertical();
        }
        setStack(fileName, stack);
    }

    private void displaySingleImage(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor imageProcessor = null;
        int dim = hdu.getAxes().length;

        if (hdu.getHeader().getIntValue(NAXIS) == 2)
        {
            imageProcessor = process2DimensionalImage(hdu, imgData);
        }
        else if (hdu.getHeader().getIntValue(NAXIS) == 3
                && hdu.getAxes()[dim - 2] == 1 && hdu.getAxes()[dim - 3] == 1)
        {
            imageProcessor = process3DimensionalImage(hdu, imgData);
        }

        if (imageProcessor == null)
        {
            imageProcessor = imagePlus.getProcessor();
            imageProcessor.flipVertical();
            setProcessor(fileName, imageProcessor);
        }
    }

    private void writeTemporaryFITSFile(BasicHDU hdu) throws FileNotFoundException, FitsException
    {
        File file = new File(IJ.getDirectory("home") + ".tmp.fits");
        FileOutputStream fis = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fis);
        fits.write(dos);
        try
        {
            wcs = new WCS(hdu.getHeader());
        }
        catch (Exception e)
        {
            Logger.getLogger(NTFITS_Reader.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(NTFITS_Reader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ImageProcessor process3DimensionalImage(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor ip;
        short[][][] itab = (short[][][]) imgData.getKernel();
        float[] xValues = new float[wi];
        float[] yValues = new float[wi];

        for (int y = 0; y < wi; y++)
        {
            yValues[y] = (float) hdu.getBZero()
                    + (float) hdu.getBScale() * itab[0][0][y];
        }

        String unitY;
        unitY = "IntensityRS ";
        String unitX;
        unitX = "FrequencyRS ";
        float CRVAL1 = 0;
        float CRPIX1 = 0;
        float CDELT1 = 0;
        if (hdu.getHeader().getStringValue("CRVAL1") != null)
        {
            CRVAL1 = Float
                    .parseFloat(hdu.getHeader().getStringValue("CRVAL1"));
        }
        if (hdu.getHeader().getStringValue("CRPIX1") != null)
        {
            CRPIX1 = Float
                    .parseFloat(hdu.getHeader().getStringValue("CRPIX1"));
        }
        if (hdu.getHeader().getStringValue("CDELT1") != null)
        {
            CDELT1 = Float
                    .parseFloat(hdu.getHeader().getStringValue("CDELT1"));
        }
        for (int x = 0; x < wi; x++)
        {
            xValues[x] = CRVAL1 + (x - CRPIX1) * CDELT1;
        }

        int div = 1;
        if (CRVAL1 > 2000000000)
        {
            div = 1000000000;
            unitX += "(Ghz)";
        }
        else if (CRVAL1 > 1000000000)
        {
            div = 1000000;
            unitX += "(Mhz)";
        }
        else if (CRVAL1 > 1000000)
        {
            div = 1000;
            unitX += "(Khz)";
        }
        else
        {
            unitX += "(Hz)";
        }

        for (int x = 0; x < wi; x++)
        {
            xValues[x] = xValues[x] / div;
        }

        // TO-DO: This constructor is deprecated!
        Plot P = new Plot(
                "PlotWinTitle" + " " + fileName,
                "X: " + unitX, "Y: " + unitY, xValues, yValues);
        P.draw();

        FloatProcessor imgtmp;
        imgtmp = new FloatProcessor(wi, he);
        imgtmp.setPixels(yValues);
        imgtmp.resetMinAndMax();

        if (he == 1)
        {
            imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
        }
        if (wi == 1)
        {
            imgtmp = (FloatProcessor) imgtmp.resize(100, he);
        }

        ip = imgtmp;

        ip.flipVertical();
        setProcessor(fileName, ip);
        return ip;
    }

    private ImageProcessor process2DimensionalImage(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor ip;//Profiler p = new Profiler();
        ///////////////////////////// 16 BITS ///////////////////////
        if (hdu.getBitPix() == 16)
        {
            short[][] itab = (short[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale() * (float) itab[y][x];
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }
            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

        } // 8 bits
        else if (hdu.getBitPix() == 8)
        {
            byte[][] itab = (byte[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    if (itab[x][y] < 0)
                    {
                        itab[x][y] += 256;
                    }
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale()
                            * (float) (itab[y][x]);
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }
            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

        } // 16-bits
        ///////////////// 32 BITS ///////////////////////
        else if (hdu.getBitPix() == 32)
        {
            int[][] itab = (int[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale() * (float) itab[y][x];
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }

            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

        } // 32 bits
        /////////////// -32 BITS ?? /////////////////////////////////
        else if (hdu.getBitPix() == -32)
        {
            float[][] itab = (float[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale() * itab[y][x];
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }

            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

            // special spectre optique transit
            if ((hdu.getHeader().getStringValue("STATUS") != null) && (hdu
                    .getHeader().getStringValue("STATUS")
                    .equals("SPECTRUM")) && (
                    hdu.getHeader().getIntValue(NAXIS) == 2))
            {
                //IJ.log("spectre optique");
                float[] xValues = new float[wi];
                float[] yValues = new float[wi];
                for (int y = 0; y < wi; y++)
                {
                    yValues[y] = itab[0][y];
                    if (yValues[y] < 0)
                    {
                        yValues[y] = 0;
                    }
                }
                String unitY;
                unitY = "IntensityRS ";
                String unitX;
                unitX = "WavelengthRS ";
                float CRVAL1 = 0;
                float CRPIX1 = 0;
                float CDELT1 = 0;
                if (hdu.getHeader().getStringValue("CRVAL1") != null)
                {
                    CRVAL1 = Float.parseFloat(
                            hdu.getHeader().getStringValue("CRVAL1"));
                }
                if (hdu.getHeader().getStringValue("CRPIX1") != null)
                {
                    CRPIX1 = Float.parseFloat(
                            hdu.getHeader().getStringValue("CRPIX1"));
                }
                if (hdu.getHeader().getStringValue("CDELT1") != null)
                {
                    CDELT1 = Float.parseFloat(
                            hdu.getHeader().getStringValue("CDELT1"));
                }
                for (int x = 0; x < wi; x++)
                {
                    xValues[x] = CRVAL1 + (x - CRPIX1) * CDELT1;
                }

                float odiv = 1;
                if (CRVAL1 < 0.000001)
                {
                    odiv = 1000000;
                    unitX += "("+"\u00B5"+"m)";
                }
                else
                {
                    unitX += "ADU";
                }

                for (int x = 0; x < wi; x++)
                {
                    xValues[x] = xValues[x] * odiv;
                }

                // TO-DO: This constructor is deprecated!
                Plot P = new Plot(
                        "PlotWinTitle "
                                + fileName, "X: " + unitX, "Y: " + unitY,
                        xValues, yValues);
                P.draw();
            } //// end of special optique
        } // -32 bits
        else
        {
            ip = imagePlus.getProcessor();
        }
        return ip;
    }

    private Data getImageData(BasicHDU hdu)
    {
        Data imgData = null;
        if (hdu.getData() != null)
        {
            imgData = hdu.getData();
        }
        return imgData;
    }

    private void buildImageDescriptionFromHeader(BasicHDU hdu)
    {
        imageDescription = "";
        if (hdu.getObject() != null)
        {
            imageDescription += "OBJECT: " + hdu.getObject() + "\n";
        }
        if (hdu.getTelescope() != null)
        {
            imageDescription += "TELESCOP: " + hdu.getTelescope()
                    + "\n";
        }
        if (hdu.getInstrument() != null)
        {
            imageDescription += "INSTRUM: " + hdu.getInstrument()
                    + "\n";
        }
        if (hdu.getHeader().getStringValue(FILTER) != null)
        {
            imageDescription += "FILTER: " + hdu.getHeader()
                    .getStringValue(FILTER) + "\n";
        }
        if (hdu.getObserver() != null)
        {
            imageDescription +=
                    "OBSERVER: " + hdu.getObserver() + "\n";
        }
        if (hdu.getHeader().getStringValue(EXPTIME) != null)
        {
            imageDescription += "EXPTIME: " + hdu.getHeader()
                    .getStringValue(EXPTIME) + "\n";
        }
        if (hdu.getObservationDate() != null)
        {
            imageDescription +=
                    "DATE-OBS: " + hdu.getObservationDate()
                            + "\n";
        }
        if (hdu.getHeader().getStringValue("UT") != null)
        {
            imageDescription += "UT: " + hdu.getHeader()
                    .getStringValue("UT") + "\n";
        }
        if (hdu.getHeader().getStringValue("UTC") != null)
        {
            imageDescription += "UT: " + hdu.getHeader()
                    .getStringValue("UTC") + "\n";
        }
        if (hdu.getHeader().getStringValue(RA) != null)
        {
            imageDescription += "RA: " + hdu.getHeader()
                    .getStringValue(RA) + "\n";
        }
        if (hdu.getHeader().getStringValue(DEC) != null)
        {
            imageDescription += "DEC: " + hdu.getHeader()
                    .getStringValue(DEC) + "\n";
        }
        imageDescription += "\n" + "\n"
                + "*************************************************************"
                + "\n";

        setProperty("Info", imageDescription + fitsDecoder.getHeaderInfo());

    }

    private void fixDimensions(BasicHDU hdu, int dim) throws FitsException
    {
        //By oli
        // replace  hdu.getAxes()[dim - 1] by fi.width
        // and replace  hdu.getAxes()[dim - 2] by fi.height
        // or it would crash if it is not defined and go to the exception case at the end
        if ((dim < 2) && (fileInfo != null))
        {
            wi = fileInfo.width;
            he = fileInfo.height;
            de = 1;
        }
        else
        {
            wi = hdu.getAxes()[dim - 1];
            he = hdu.getAxes()[dim - 2];
            if (dim > 2)
            {
                de = hdu.getAxes()[dim - 3];
            }
            else
            {
                de = 1;
            }
        }
    }

    private BasicHDU[] getHDU(String path) throws FitsException
    {
        OpenDialog od = new OpenDialog("Open FITS...", path);
        String directory = od.getDirectory();
        fileName = od.getFileName();
        if (fileName == null)
        {
            throw new FitsException("Null filename.");
        }
        IJ.showStatus("Opening: " + directory + fileName);
        IJ.log("Opening: " + directory + fileName);
        fitsDecoder = new FITSDecoder(directory, fileName);
        fileInfo = null;
        try
        {
            fileInfo = fitsDecoder.getInfo();
        }
        catch (IOException e)
        {
            throw new FitsException("Failed to create FITS decoder: " + e.getMessage());
        }

        fits = new Fits(directory + fileName);

        try
        {
            return fits.read();
        }
        catch (Exception e)
        {
            throw new FitsException("Failed to read FITS file using nom.tam.fits: " + e.getMessage());
        }
    }

    /**
     * Gets the locationAsString attribute of the FITS object
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @return The locationAsString value
     */
    public String getLocationAsString(int x, int y)
    {
        String s;
        if (wcs != null)
        {
            double[] in = new double[2];
            in[0] = (double) (x);
            in[1] = getProcessor().getHeight() - y - 1.0;
            //in[2]=0.0;
            double[] out = wcs.inverse().transform(in);
            double[] coord = new double[2];
            skyview.geometry.Util.coord(out, coord);
            CoordinateFormatter cf = new CoordinateFormatter();
            String[] ra = cf.sexagesimal(Math.toDegrees(coord[0]) / 15.0, 8).split(" ");
            String[] dec = cf.sexagesimal(Math.toDegrees(coord[1]), 8).split(" ");

            s = "x=" + x + ",y=" + y + " (RA=" + ra[0] + "h" + ra[1] + "m" + ra[2] + "s,  DEC="
                    + dec[0] + "\u00b0"+" " + dec[1] + "' " + dec[2] + "\"" + ")";

        }
        else
        {
            s = "x=" + x + " y=" + y;
        }
        if (getStackSize() > 1)
        {
            s += " z=" + (getCurrentSlice() - 1);
        }
        return s;
    }
}