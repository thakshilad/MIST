package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.gui.viewers.transactionviewer.TransactionDatabaseViewer;
/*
 * Copyright (c) 2024 Philippe Fournier-Viger
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Example of how to use Transaction Database viewer
 *  algorithm from the source code.
 * @author Philippe Fournier-Viger (Copyright 2024)
 */
public class MainTestTransactionViewer {

	public static void main(String [] arg) throws IOException{

//		String input = fileToPath("contextPasquierWithNames.txt");
		String input = fileToPath("contextPasquier99.txt");

		// Applying the viewer
		TransactionDatabaseViewer viewer = new TransactionDatabaseViewer(true, input);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
//		System.out.println("filename : " + filename);
		URL url = MainTestTransactionViewer.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
