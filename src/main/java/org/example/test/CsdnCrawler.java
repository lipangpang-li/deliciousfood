package org.example.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class CsdnCrawler {
    private static final String URL = "https://blog.csdn.net/qq_22075913/article/details/145529196";
    private static final Pattern MULTI_NEWLINE = Pattern.compile("\n{3,}");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .header("Referer", "https://blog.csdn.net/")
                    .timeout(15000)
                    .get();

            Element content = doc.selectFirst("div.markdown_views");
            if (content == null) {
                System.out.println("[错误] 内容容器未找到");
                return;
            }

            String structuredContent = processStructuredContent(content);
            String savePath = "C:\\Users\\ASUS\\Desktop\\面试题总结\\csdn\\interview_questions.docx";
            createStructuredWordDocument(savePath, structuredContent);

        } catch (IOException | InvalidFormatException e) {
            System.err.println("[系统错误] " + e.getMessage());
            e.printStackTrace();
        }
    }

    //=============== 内容结构化处理 ===============//
    private static String processStructuredContent(Element content) {
        List<Node> nodes = content.childNodes();
        StringBuilder sb = new StringBuilder();

        for (Node node : nodes) {
            if (node instanceof Element) {
                Element el = (Element) node;
                if (isCodeBlock(el)) {
                    processCodeBlock(el, sb);
                } else if (isImage(el)) {
                    processImage(el, sb);
                } else {
                    processTextElement(el, sb);
                }
            }
        }

        String text = sb.toString()
                .replaceAll("\u00a0", " ")
                .replaceAll("\\s+\n", "\n");

        return MULTI_NEWLINE.matcher(text).replaceAll("\n\n");
    }

    private static boolean isCodeBlock(Element el) {
        return "pre".equalsIgnoreCase(el.tagName()) && el.selectFirst("code") != null;
    }

    private static boolean isImage(Element el) {
        return "img".equalsIgnoreCase(el.tagName()) && el.hasAttr("src");
    }

    private static void processCodeBlock(Element el, StringBuilder sb) {
        String code = el.selectFirst("code").text();
        sb.append("[CODE_BLOCK]").append(code).append("[/CODE_BLOCK]\n");
    }

    private static void processImage(Element el, StringBuilder sb) {
        String imgUrl = el.absUrl("src");
        sb.append("[IMAGE]").append(imgUrl).append("[/IMAGE]\n");
    }

    private static void processTextElement(Element el, StringBuilder sb) {
        String text = el.text().trim();
        if (!text.isEmpty()) {
            sb.append(text).append("\n");
        }
    }

    //=============== Word文档生成 ===============//
    private static void createStructuredWordDocument(String path, String content)
            throws IOException, InvalidFormatException {

        Files.createDirectories(Paths.get(path).getParent());

        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(path)) {

            addMainTitle(doc, "CSDN面试题整理");
            processContentSections(doc, content);
            doc.write(out);
            System.out.println("[成功] 文档已生成: " + path);
        }
    }

    private static void addMainTitle(XWPFDocument doc, String title) {
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = titlePara.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(18);
        titlePara.setSpacingAfter(400);
    }

    private static void processContentSections(XWPFDocument doc, String content)
            throws IOException, InvalidFormatException {

        String[] sections = content.split("\n");
        for (String section : sections) {
            if (section.startsWith("[CODE_BLOCK]")) {
                handleCodeBlock(doc, section);
            } else if (section.startsWith("[IMAGE]")) {
                handleImage(doc, section);
            } else if (!section.trim().isEmpty()) {
                handleNormalText(doc, section);
            }
        }
    }

    private static void handleCodeBlock(XWPFDocument doc, String section) {
        String code = section.replace("[CODE_BLOCK]", "").replace("[/CODE_BLOCK]", "");
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        para.setIndentationLeft(400);

        XWPFRun run = para.createRun();
        run.setFontFamily("Courier New");
        run.setFontSize(10);
        run.setText(code);
        para.setBorderBottom(Borders.SINGLE);
    }

    private static void handleImage(XWPFDocument doc, String section) throws InvalidFormatException {
        try {
            String imgUrl = section.replace("[IMAGE]", "").replace("[/IMAGE]", "");
            System.out.println("[下载] 正在获取图片: " + imgUrl);

            Thread.sleep(1000 + RANDOM.nextInt(2000)); // 反爬延迟

            byte[] imageData = Jsoup.connect(imgUrl)
                    .ignoreContentType(true)
                    .userAgent(USER_AGENT)
                    .referrer(URL)
                    .maxBodySize(10 * 1024 * 1024)
                    .execute()
                    .bodyAsBytes();

            addImageToDocument(doc, imageData);

        } catch (IOException e) {
            System.err.println("[图片错误] 下载失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void addImageToDocument(XWPFDocument doc, byte[] imageData)
            throws InvalidFormatException, IOException {

        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            XWPFParagraph para = doc.createParagraph();
            para.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun run = para.createRun();
            run.addPicture(
                    bis,
                    detectImageType(imageData),
                    "image_" + System.currentTimeMillis(),
                    Units.toEMU(500),
                    Units.toEMU(375)
            );
        }
    }

    private static int detectImageType(byte[] imageData) {
        if (imageData.length > 4) {
            // PNG: 89 50 4E 47
            if (imageData[1] == 0x50 && imageData[2] == 0x4E && imageData[3] == 0x47) {
                return XWPFDocument.PICTURE_TYPE_PNG;
            }
            // JPEG: FF D8
            if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                return XWPFDocument.PICTURE_TYPE_JPEG;
            }
        }
        return XWPFDocument.PICTURE_TYPE_PNG; // 默认类型
    }

    private static void handleNormalText(XWPFDocument doc, String text) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setFontSize(12);
        para.setSpacingAfter(100);
    }
}
