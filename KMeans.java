/*** Author :Vibhav Gogate
The University of Texas at Dallas
*****/
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class KMeans {
public static void main(String [] args){
	int[] KValues = {2, 5, 10, 15, 20};
	String[] fileNames = {"Koala", "Penguins"};
	int nRuns = 5;
	for(int fileNameIdx = 0; fileNameIdx < fileNames.length; fileNameIdx++) {
		File inputFile = new File(fileNames[fileNameIdx]+".jpg");
		double compression[][] = new double[KValues.length][nRuns];
		for(int kValIdx = 0; kValIdx < KValues.length; kValIdx++) {
			int k = KValues[kValIdx];
		for(int run = 0; run < nRuns; run++){
			try {
			BufferedImage originalImage = ImageIO.read(inputFile);

			BufferedImage kmeansJpg = kmeans_helper(originalImage,k);
			File outputFile = new File(fileNames[fileNameIdx]+"CompressedRun"+run+"K"+k+".jpg");
			
			ImageIO.write(kmeansJpg, "jpg", outputFile); 
			
			double inputBytes = inputFile.length();
			double inputFileSize = inputBytes/1024;
			//System.out.println("Input Size: " + inputFileSize);
			
			double outputBytes = outputFile.length();
			double outputFileSize = outputBytes/1024;
			//System.out.println("Output Size: " + outputFileSize);
			
			compression[kValIdx][run] = inputFileSize/outputFileSize;

			System.out.println("FileName: " + inputFile.getName() + " K:" +k + " Compression: " +compression[kValIdx][run]);
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}	
	} // run
} // kValue
System.out.println("FileName: "+ inputFile.getName());
for(int cIdx = 0; cIdx < compression.length; cIdx++) {
	double cSum = 0;
	
	for(int r = 0; r<compression[cIdx].length; r++) {
		cSum += compression[cIdx][r];
	}
	double mean = (cSum/nRuns);
	System.out.println("Mean for K value of " + KValues[cIdx] + " is:" + mean);
	double varSum = 0;
	for(int r = 0; r<compression[cIdx].length; r++) {
		varSum += Math.pow((compression[cIdx][r]-mean),2);
	}
	double var = varSum/nRuns;
	System.out.println("Variance for K value of" + KValues[cIdx] + " is: " +String.format("%.6f", var));
}

} // Filename
}
    
    private static BufferedImage kmeans_helper(BufferedImage originalImage, int k){
	int w=originalImage.getWidth();
	int h=originalImage.getHeight();
	BufferedImage kmeansImage = new BufferedImage(w,h,originalImage.getType());
	Graphics2D g = kmeansImage.createGraphics();
	g.drawImage(originalImage, 0, 0, w,h , null);
	// Read rgb values from the image
	double[][] rgb=new double[w*h][3];
	int count=0;
	for(int i=0;i<w;i++){
	    for(int j=0;j<h;j++){
		Color color1 = new Color(kmeansImage.getRGB(i, j));
            //Retrieving the R G B values
            double red = color1.getRed();
            double green = color1.getGreen();
            double blue = color1.getBlue();
            rgb[count][0] = red;
            rgb[count][1] = green;
            rgb[count][2] = blue; 
            count++;
	    }
	}

	// Call kmeans algorithm: update the rgb values
	kmeans(rgb,k);

	// Write the new rgb values to the image
	count=0;
	for(int i=0;i<w;i++){
	    for(int j=0;j<h;j++){
            int alpha = 255; 
            
            int red   = (int)rgb[count][0];
            int green = (int)rgb[count][1];
            int blue  = (int)rgb[count][2];
            
            int col = alpha << 24 | red << 16 | green << 8 | blue;
            kmeansImage.setRGB(i,j,col);
            count++;
	    }
	
    }

	return kmeansImage;
    }

    private static double computeDistance(double c1, double c2, double c3, double x1, double x2, double x3) {
        return Math.sqrt(Math.pow(c1-x1, 2) + Math.pow(c2-x2, 2) + Math.pow(c3-x3,2));
    }
	private static int assignCluster(double[][] clusterCenters, double[] dataPoint) {
		// Given cluster center and datapoint
		// Compute nearest cluster and assign to it
		double minDistance = Double.MAX_VALUE;
		int clusterAssigned = 0;
		for (int clusterId = 0; clusterId < clusterCenters.length; clusterId++) {
            double[] currentCenter = clusterCenters[clusterId];
			double distance = computeDistance(currentCenter[0], currentCenter[1], currentCenter[2], dataPoint[0], dataPoint[1], dataPoint[2]);
			if(distance < minDistance) {
				minDistance = distance;
				clusterAssigned = clusterId;
			}
		}
		return clusterAssigned;
	}

	private static double[] calculateMean(ArrayList<Integer> indexes, double[][] rgb) {
		// Calculate mean of all elements in the cluster and return it.
		double sum[] = {0,0,0};
		
		for (int elemIndex = 0; elemIndex < indexes.size(); elemIndex++) {
			double[] rgbElem = rgb[indexes.get(elemIndex)];
            sum[0]  += (rgbElem[0])/indexes.size();
            sum[1]  += (rgbElem[1])/indexes.size();
            sum[2]  += (rgbElem[2])/indexes.size();
		}
		return sum;
	}

	private static void recomputeCenters(double[][] clusterCenters, Map<Integer, ArrayList<Integer>> clusters, double[][] rgb, int centersChanged) {
		// Calculate cluster elements mean and set it as new cluster center
		for (int clusterId = 0; clusterId < clusterCenters.length; clusterId++) {
			ArrayList<Integer> clusterElemsIndexes = clusters.get(clusterId);
			if(clusterElemsIndexes != null) { 
				double[] newCenter = calculateMean(clusterElemsIndexes, rgb);
				if (clusterCenters[clusterId] != newCenter)
				{ 
					clusterCenters[clusterId] = newCenter;
					centersChanged++;
				}
			}
		}
	}

	private static Map<Integer, ArrayList<Integer>> recomputeClusters(double[][] clusterCenters, double[][] rgb) {
		Map<Integer, ArrayList<Integer>> clusterMappings = new HashMap<>();
		// Assign a given data point to a cluster based on it's distance
		// Return the new info.
		for(int dataPointId = 0; dataPointId < rgb.length; dataPointId++) {
			int clusterAssigned = assignCluster(clusterCenters, rgb[dataPointId]);
		
			if(clusterMappings.containsKey(clusterAssigned)) {
				ArrayList<Integer> oldVal = clusterMappings.get(clusterAssigned);
				oldVal.add(dataPointId);
				clusterMappings.replace(clusterAssigned, oldVal);
			}
			else {
				ArrayList<Integer> clusterElem = new ArrayList<>();
				clusterElem.add(dataPointId);
				clusterMappings.put(clusterAssigned, clusterElem);
			}
		}
		return clusterMappings;
	}
	
    private static void kmeans(double[][] rgb, int k) {
		
		double[][] clusterCenters = new double[k][3];

		Random rand = new Random();
		for(int clusterId = 0; clusterId < k; clusterId++) {
			clusterCenters[clusterId] = rgb[rand.nextInt(rgb.length)];
		}
       
		// Compute euclidean distances to the cluster centers from remaining elements
		// Assign it to the cluster which ever it is closest to.
		Map<Integer, ArrayList<Integer>> clusterMap = new HashMap<>();
		int centersChanged = k;
		//while(centersChanged!=0){
		for(int iter = 0; iter<20; iter++){
			centersChanged = 0;
			clusterMap = recomputeClusters(clusterCenters, rgb);
			recomputeCenters(clusterCenters, clusterMap, rgb, centersChanged);	
		}
	
		// Updating the RGB values
		clusterMap.forEach((clusterCenter,indexes) -> {
			for(int idx = 0; idx < indexes.size(); idx++) {
            double[] currentCenter = clusterCenters[clusterCenter];
			rgb[indexes.get(idx)][0] = currentCenter[0];
            rgb[indexes.get(idx)][1] = currentCenter[1];
            rgb[indexes.get(idx)][2] = currentCenter[2];
			}
		});    
	}

}


//javac KMeans.java
//java KMeans Koala 5 K
    
