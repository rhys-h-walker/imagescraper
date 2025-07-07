package com.rwalker.github;

import com.microsoft.playwright.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImageScraper {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ImageScraper <URL> <output-folder>");
            return;
        }

        String url = args[0];
        String outputFolder = args[1];

        String html = getHTMLAsString(url);
        List<String> links = extractImageUrls(html);

        int nameCounter = 0;
        for (String imageUrl : links) {
            if (isSupportedImage(imageUrl)) {
                downLoadImage(imageUrl, String.valueOf(nameCounter), outputFolder);
                nameCounter++;
            } else {
                System.out.println("Skipping unsupported image type: " + imageUrl);
            }
        }
    }

    public static String getHTMLAsString(String url) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();
            page.navigate(url);

            page.waitForSelector("img");

            String html = page.content();

            browser.close();
            return html;
        }
    }

    public static List<String> extractImageUrls(String html) {
        List<String> imageUrls = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements imgTags = doc.select("img");

        for (Element img : imgTags) {
            String src = img.attr("src");
            if (!src.isEmpty()) {
                imageUrls.add(src);
            }
        }

        return imageUrls;
    }

    public static void downLoadImage(String URL, String filename, String outputFolder) {
        String url = URL;

        File dir = new File(outputFolder);
        if (!dir.exists()) {
            dir.mkdirs(); // create the folder and any necessary parent directories
        }

        if (!url.substring(0, 6).equals("https:")) {
            url = "https:"+url;
        }

        try {
            URL imageUrl = new URL(url);
            BufferedImage image = ImageIO.read(imageUrl);
            ImageIO.write(image, "jpg", new File(outputFolder+"/"+filename+".jpg"));
            System.out.println("Image downloaded successfully: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSupportedImage(String imageUrl) {
        // Extract the extension (everything after the last dot)
        String lowercaseUrl = imageUrl.toLowerCase();

        int lastDotIndex = lowercaseUrl.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == lowercaseUrl.length() - 1) {
            return false; // no extension or dot is last character
        }

        String extension = lowercaseUrl.substring(lastDotIndex + 1);
        Set<String> supportedExtensions = Set.of("jpg", "jpeg", "png", "bmp", "gif", "wbmp");

        return supportedExtensions.contains(extension);
    }
}
