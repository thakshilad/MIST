package ca.pfv.spmf.algorithmmanager.descriptions;

import java.io.IOException;

import ca.pfv.spmf.algorithmmanager.AlgorithmType;
import ca.pfv.spmf.algorithmmanager.DescriptionOfAlgorithm;
import ca.pfv.spmf.algorithmmanager.DescriptionOfParameter;
import ca.pfv.spmf.algorithms.associationrules.IGB.AlgoIGB;
/* This file is copyright (c) 2008-2016 Philippe Fournier-Viger
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
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import ca.pfv.spmf.algorithms.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;

/**
 * This class describes parameters of the algorithm for generating association rules 
 * with the IGB algorithm. 
 * It is designed to be used by the graphical and command line interface.
 * 
 * @see AlgoIGB
 * @author Philippe Fournier-Viger
 */
public class DescriptionAlgoIGB extends DescriptionOfAlgorithm {

	/**
	 * Default constructor
	 */
	public DescriptionAlgoIGB(){
	}

	@Override
	public String getName() {
		return "IGB";
	}

	@Override
	public String getAlgorithmCategory() {
		return "ASSOCIATION RULE MINING";
	}

	@Override
	public String getURLOfDocumentation() {
		return "http://philippe-fournier-viger.com/spmf/IGBAssociationRules.php";
	}

	@Override
	public void runAlgorithm(String[] parameters, String inputFile, String outputFile) throws IOException {
		double minsup = getParamAsDouble(parameters[0]);
		double minconf = getParamAsDouble(parameters[1]);
		
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(inputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Applying the Zart algorithm
		AlgoZart zart = new AlgoZart();
		TZTableClosed results = zart.runAlgorithm(database, minsup);
		zart.printStatistics();
		
		System.out.println("STEP 2 : RUNNING THE IGB ALGORITHM");
		// Apply the IGB algorithm
		AlgoIGB algoIGB = new AlgoIGB();
		algoIGB.runAlgorithm(results, database.getTransactions().size(), minconf, outputFile);
		algoIGB.printStatistics();

//		AlgoIGB apriori = new AlgoIGB();
//		ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = apriori
//				.runAlgorithm(minsup, inputFile, null);
//		apriori.printStats();
//		int databaseSize = apriori.getDatabaseSize();
//
//		// STEP 2: Generating all rules from the set of frequent itemsets
//		// (based on Agrawal & Srikant, 94)
//		ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
//		algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize,
//				minconf);
//		algoAgrawal.printStats();
	}

	@Override
	public DescriptionOfParameter[] getParametersDescription() {
        
		DescriptionOfParameter[] parameters = new DescriptionOfParameter[2];
		parameters[0] = new DescriptionOfParameter("Minsup (%)", "(e.g. 0.5 or 50%)", Double.class, false);
		parameters[1] = new DescriptionOfParameter("Minconf (%)", "(e.g. 0.61 or 61%)", Double.class, false);
		return parameters;
	}

	@Override
	public String getImplementationAuthorNames() {
		return "Philippe Fournier-Viger";
	}

	@Override
	public String[] getInputFileTypes() {
		return new String[]{"Database of instances","Transaction database", "Simple transaction database"};
	}

	@Override
	public String[] getOutputFileTypes() {
		return new String[]{"Patterns", "Association rules"};
	}
	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.DATA_MINING;
	}
}
