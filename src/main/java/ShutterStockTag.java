import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.exif.Exif;
import com.icafe4j.image.meta.jpeg.JpegExif;
import com.icafe4j.image.tiff.FieldType;
import com.icafe4j.image.tiff.TiffTag;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShutterStockTag {
	private static Logger logger = LoggerFactory.getLogger(ShutterStockTag.class);
	private static final String SHUTTER_IMAGE_URL = "https://www.shutterstock.com/ru/image-photo/";
	private static final String SHUTTER_DIV_TAGS = "ExpandableKeywordsList_container_div";
	private static final String TAGGED_PHOTO_PREFIX = "shutterstock_tagged_";

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			logger.info("Not enough params, please specify folder where to put tags on photos, ex: C:\\User\\Photos");
		}
		String mainPath = args[0];
		String pathWithTags = mainPath + "/tagged";
		File dir = new File(pathWithTags);
		if (!dir.exists()) dir.mkdirs();
		List<Path> jpgFiles = Files.walk(Paths.get(args[0])).filter(p -> (p.toString().endsWith(".jpg") || p.toString().endsWith(".jpeg"))).collect(Collectors.toList());

		if (jpgFiles.isEmpty()) {
			logger.info("Cannot find any jpg file inside " + mainPath + ", please check directory");
		} else {
			logger.info("Found " + jpgFiles.size() + " jpg files, files with tags will be placed inside " + pathWithTags);
		}

		jpgFiles.forEach(image -> {
			logger.info("Start parse tags for " + image.getFileName());
			String[] splitUnderScore = image.getFileName().toString().split("_");
			if (splitUnderScore.length != 2) {
				logger.info("File " + image.getFileName() + " does not respect naming convention, skipping");
				return;
			}
			String[] splitPoint = splitUnderScore[1].split("\\.");
			if (splitPoint.length != 2) {
				logger.info("File " + image.getFileName() + " does not respect naming convention, skipping");
				return;
			}
			String imageId = splitPoint[0];

			try {
				Document doc = Jsoup.connect(SHUTTER_IMAGE_URL + imageId).followRedirects(true).get();
				Element mainElement = findMainElement(doc);
				if (mainElement != null) {
					List<String> tags = mainElement.getAllElements().stream().filter(element -> element.tagName().equals("a")).map(Element::text).collect(Collectors.toList());
					if (!tags.isEmpty()) {
						logger.info(tags.size() + " new tags will be added for " + imageId);
						File newImage = new File(pathWithTags + "/" + TAGGED_PHOTO_PREFIX + imageId + "." + FilenameUtils.getExtension(image.getFileName().toString()));
						changeExifMetadata(image.toFile(), newImage, tags);
					}
				} else {
					logger.info("Cannot find tags to parse");
				}
			} catch (Exception ignored) {
				logger.info("Something went wrong while parsing html content for image " + image.getFileName());
			}
		});
		logger.info("Finish");
	}

	public static Element findMainElement(Document doc) {
		for( Element element : doc.getAllElements()) {
			for( Attribute attribute : element.attributes()) {
				if( attribute.getValue().equalsIgnoreCase(SHUTTER_DIV_TAGS)) {
					return element;
				}
			}
		}
		return null;
	}

	public static void changeExifMetadata(final File jpegImageFile, final File dst, List<String> tags) throws Exception {
		FileInputStream inputStream = new FileInputStream(jpegImageFile);
		FileOutputStream outputStream = new FileOutputStream(dst);
		List<Metadata> metaList = new ArrayList<>();
		Exif exif = new JpegExif();
		exif.addImageField(TiffTag.WINDOWS_XP_KEYWORDS, FieldType.WINDOWSXP, String.join(";", tags));
		exif.setThumbnailRequired(true);
		metaList.add(exif);
		Metadata.insertMetadata(metaList, inputStream, outputStream);
		inputStream.close();
		outputStream.close();
	}
}