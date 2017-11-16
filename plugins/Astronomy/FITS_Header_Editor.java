// FITS_Header_Editor.java

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import astroj.FitsHeaderEditor;

public class FITS_Header_Editor implements PlugInFilter
	{
	ImagePlus imp;

    
	public int setup(String arg, ImagePlus imp)
		{
		this.imp = imp;
		return DOES_ALL;
		}

	public void run(ImageProcessor ip)
		{
		// PLACE FITS HEADER IN A STRING ARRAY
        FitsHeaderEditor fhe = new FitsHeaderEditor(imp);
        }
 	}
