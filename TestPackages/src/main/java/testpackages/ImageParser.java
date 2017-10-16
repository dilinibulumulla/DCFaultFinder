package testpackages;

/** * sample program to read images belonging to a sample Class e.g. Male/Female . Then writing the images to Vector format into a text file. These text files will be used by Apache Spark for Linear SVM analysis */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * @author smruti
 *
 */
public class ImageParser {

	/**
	 * @param args
	 */

	public static void toGray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				int red = (int) (c.getRed() * 0.21);
				int green = (int) (c.getGreen() * 0.72);
				int blue = (int) (c.getBlue() * 0.07);
				int sum = red + green + blue;
				Color newColor = new Color(sum, sum, sum);
				image.setRGB(j, i, newColor.getRGB());
			}
		}
	}

	public static BufferedImage createResizedCopy(BufferedImage originalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		System.out.println("resizing...");
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}

	public static void main(String[] args) throws IOException {

		/* args[0] is the pixel to which the image will be converted */
		// String[] piexels = args[0].split("x");

		// int scaledWidth = Integer.parseInt(piexels[0]);
		// int scaledHeight = Integer.parseInt(piexels[1]);

		int[] piexels = { 180, 200 };
		int scaledWidth = piexels[0];
		int scaledHeight = piexels[1];

		int gender = 0; // 1 is male 0 is female

		String locationOfFiles = null;
		if (gender == 1) {
			locationOfFiles = "data/faces96/male";// testImages
		} else if (gender == 0) {
			locationOfFiles = "data/faces96/testImages/female";// testImages
		} else {
			System.err.println("Not a valid file location");
			System.exit(1);
		}

		ArrayList<String> paths = new ArrayList<String>();

		/*
		 * Traverse All the files inside the Folder and sub folder. args[1] is
		 * the path of the folder having the images
		 */
		// Files.walkFileTree(Paths.get(args[1].toString()), new
		// SimpleFileVisitor<Path>() {
		Files.walkFileTree(Paths.get(locationOfFiles), new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// Files.delete(file);
				paths.add(file.toFile().getAbsolutePath());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				// Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});

		for (String file : paths) {

			String extension = "";
			String labelName;

			int extIndex = file.indexOf('.');
			int startIndex = file.indexOf("female/");

			extension = file.substring(extIndex + 1);
			// labelName = file.substring(startIndex + 15, extIndex);

			System.out.println(file + " extension " + extension + " string to int ");// +
			// extension

			File input = new File(file.toString());
			BufferedImage image = createResizedCopy(ImageIO.read(input), scaledWidth, scaledHeight, Boolean.TRUE);
			toGray(image);
			File output = new File(file.toString());
			ImageIO.write(image, extension, output);
		}

		try {
			/*
			 * args[2] is the Class of the image, Class = Male/Female. Vector
			 * will be written into a text file
			 */
			try (PrintWriter out = new PrintWriter(
					new BufferedWriter(new FileWriter("data/inputTest-" + gender + ".csv", true)))) {

				for (String file : paths) {
					String labelName;
					int extIndex = file.indexOf('.');
					int startIndex = file.indexOf("female/");
					// labelName = file.substring(startIndex + 15, extIndex);

					File file1 = new File(file.toString());
					BufferedImage img = ImageIO.read(file1);
					if (img == null)
						continue;
					Raster raster = img.getData();
					int w = raster.getWidth(), h = raster.getHeight();
					// out.print(file1.getName());
					out.print(gender);
					out.print("," + file1.getName() + ",");
					for (int x = 0; x < w; x++) {
						for (int y = 0; y < h; y++) {
							out.print(raster.getSample(x, y, 0) + " ");
						}
						out.print(" ");
					}
					out.println("");
				}

			} catch (IOException e) {
				// exception handling skipped for the reader
			}

		} catch (Exception e) {
			// exception handling skipped for the reader
		}

	}

	static class ImageClass {
		private String idString;
		private int gender;

	}
}
