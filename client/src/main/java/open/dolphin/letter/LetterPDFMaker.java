package open.dolphin.letter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import open.dolphin.client.ClientContext;
import open.dolphin.helper.UserDocumentHelper;
import open.dolphin.project.Project;

/**
 * 紹介状の PDF メーカー。
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class LetterPDFMaker extends AbstractLetterPDFMaker {

    private static final int ADDRESS_FONT_SIZE = 9;

    @Override
    public String create() {

        try {
            Document document = new Document(
                    PageSize.A4,
                    getMarginLeft(),
                    getMarginRight(),
                    getMarginTop(),
                    getMarginBottom());
            
            String DOC_TITLE = ClientContext.getMyBundle(LetterPDFMaker.class).getString("title.refferalLeter");
            
            String path = UserDocumentHelper.createPathToDocument(
                    getDocumentDir(),       // PDF File を置く場所
                    DOC_TITLE,              // 文書名
                    EXT_PDF,                // 拡張子 
                    model.getPatientName(), // 患者氏名 
                    new Date());            // 日付
           
            Path pathObj = Paths.get(path);
            setPathToPDF(pathObj.toAbsolutePath().toString());         // 呼び出し側で取り出せるように保存する
            PdfWriter.getInstance(document, Files.newOutputStream(pathObj));
            document.open();            
            // Font
            baseFont = BaseFont.createFont(HEISEI_MIN_W3, UNIJIS_UCS2_HW_H, false);
            titleFont = new Font(baseFont, getTitleFontSize());
            bodyFont = new Font(baseFont, getBodyFontSize());
            Font addressFont = new Font(baseFont, ADDRESS_FONT_SIZE);

            // タイトル
            Paragraph para = new Paragraph(DOC_TITLE, titleFont);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);

            // 日付
            String dateStr = getDateString(model.getStarted());        
            para = new Paragraph(dateStr, bodyFont);
            para.setAlignment(Element.ALIGN_RIGHT);
            document.add(para);

            document.add(new Paragraph("　"));

            // 紹介先病院
            Paragraph para2 = new Paragraph(model.getConsultantHospital(), bodyFont);
            para2.setAlignment(Element.ALIGN_LEFT);
            document.add(para2);

            // 紹介先診療科
            para2 = new Paragraph(model.getConsultantDept(), bodyFont);
            para2.setAlignment(Element.ALIGN_LEFT);
            document.add(para2);

            // 紹介先医師
            StringBuilder sb = new StringBuilder();
            if (model.getConsultantDoctor()!= null) {
                sb.append(model.getConsultantDoctor());
                sb.append(" ");
            }
            sb.append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("doctorTitle")).append(" ");
            // title
            String title = Project.getString("letter.atesaki.title");
            if (title!=null && (!title.equals("無し"))) {
                sb.append(" ").append(title);
            }
            para2 = new Paragraph(sb.toString(), bodyFont);
            para2.setAlignment(Element.ALIGN_LEFT);
            document.add(para2);

            // 紹介元病院
            para2 = new Paragraph(model.getClientHospital(), bodyFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);

//            // 紹介元診療科
//            para2 = new Paragraph(model.getCl, bodyFont);
//            para2.setAlignment(Element.ALIGN_RIGHT);
//            document.add(para2);

            // 紹介元医師
            sb = new StringBuilder();
            sb.append(model.getClientDoctor());
            sb.append(" ").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.seal"));
            para2 = new Paragraph(sb.toString(), bodyFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);

// masuda 紹介元住所 稲葉先生のリクエスト　郵便番号を含める^
            sb = new StringBuilder();
            String clientZip = model.getClientZipCode();
            if (clientZip != null && !clientZip.isEmpty()) {
                sb.append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("mark.zipCode"));
                sb.append(clientZip);
                sb.append(" ");
            }
            sb.append(model.getClientAddress());
            para2 = new Paragraph(sb.toString(), addressFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);   
//            para2 = new Paragraph(model.getClientAddress(), bodyFont);
//            para2.setAlignment(Element.ALIGN_RIGHT);
//            document.add(para2);

// masuda 紹介元電話番号 稲葉先生のリクエスト　Faxを含める^
            sb = new StringBuilder();
            sb.append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.telephone"));
            sb.append(model.getClientTelephone());
            String fax = model.getClientFax();
            if (fax != null && !fax.isEmpty()) {
                sb.append(" ").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.fax"));
                sb.append(fax);
            }
            para2 = new Paragraph(sb.toString(), addressFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);
            
//            sb = new StringBuilder();
//            sb.append("電話 ");
//            sb.append(model.getClientTelephone());
//            para2 = new Paragraph(sb.toString(), bodyFont);
//            para2.setAlignment(Element.ALIGN_RIGHT);
//            document.add(para2);
//            
//            // 紹介元FAX番号
//            if (model.getClientFax()!=null) {
//                sb = new StringBuilder();
//                sb.append("FAX ");
//                sb.append(model.getClientFax());
//                para2 = new Paragraph(sb.toString(), bodyFont);
//                para2.setAlignment(Element.ALIGN_RIGHT);
//                document.add(para2);
//            }

            document.add(new Paragraph("　"));

            // 紹介挨拶
            if (Project.getBoolean("letter.greetings.include")) {
                String GREETINGS = ClientContext.getMyBundle(LetterPDFMaker.class).getString("greetings.letter");
                para2 = new Paragraph(GREETINGS, bodyFont);
                para2.setAlignment(Element.ALIGN_CENTER);
                document.add(para2);
            }

            // 患者
            Table pTable = new Table(4);
            pTable.setPadding(2);
            int width[] = new int[]{20, 60, 10, 10};
            pTable.setWidths(width);
            pTable.setWidth(100);

            String birthday = getDateString(model.getPatientBirthday());
            String sexStr = getSexString(model.getPatientGender());
            pTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.patinetName"), bodyFont));
            pTable.addCell(new Phrase(model.getPatientName(), bodyFont));
            pTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.patientGender"), bodyFont));
            pTable.addCell(new Phrase(sexStr, bodyFont));
            pTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.birthDate"), bodyFont));
            sb = new StringBuilder();
            sb.append(birthday).append(" ").append("(");
            sb.append(model.getPatientAge());
            sb.append(" ").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.age")).append(")");
            Cell cell = new Cell(new Phrase(sb.toString(), bodyFont));
            cell.setColspan(3);
            pTable.addCell(cell);
            //document.add(pTable);
            
            // 稲葉先生のリクエスト 患者住所と電話番号を含める
//s.oh^ 2013/11/26 文書の電話出力対応
            if(Project.getBoolean(Project.LETTER_TELEPHONE_OUTPUTPDF)) {
//s.oh$
                pTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.addressAndTelephone"), bodyFont));
            }else{
                pTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.address"), bodyFont));
            }
            sb = new StringBuilder();
            // LetterModelには住所などは含まれていないのでChartから取得する-> X
            try {

                String zipCode = model.getPatientZipCode();
                if (zipCode != null && !"".equals(zipCode)) {
                    sb.append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("mark.zipCode"));
                    sb.append(zipCode);
                    sb.append(" ");
                }

                String address = model.getPatientAddress();
                if (address != null && !"".equals(address)) {
                    address = address.replace(" ", "");
                    sb.append(address);
                }

//s.oh^ 2013/11/26 文書の電話出力対応
                if(Project.getBoolean(Project.LETTER_TELEPHONE_OUTPUTPDF)) {
//s.oh$
                    String telephone = model.getPatientTelephone();
                    if (telephone != null && !"".equals(telephone)) {
                        sb.append(" ").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.telephone"));
                        sb.append(telephone);
                    }
                }

                cell = new Cell(new Phrase(sb.toString(), bodyFont));
                cell.setColspan(3);
                pTable.addCell(cell);

            } catch (NullPointerException e) {
            }
            document.add(pTable);

            // 紹介状内容
            String disease = model.getItemValue(LetterImpl.ITEM_DISEASE);
            String purpose = model.getItemValue(LetterImpl.ITEM_PURPOSE);
            String pastFamily = model.getTextValue(LetterImpl.TEXT_PAST_FAMILY);
            String clinicalCourse = model.getTextValue(LetterImpl.TEXT_CLINICAL_COURSE);
            String medication = model.getTextValue(LetterImpl.TEXT_MEDICATION);
            String remarks = model.getItemValue(LetterImpl.ITEM_REMARKS);

            Table lTable = new Table(2); //テーブル・オブジェクトの生成
            lTable.setPadding(2);
            width = new int[]{20, 80};
            lTable.setWidths(width); //各カラムの大きさを設定（パーセント）
            lTable.setWidth(100);

            lTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.dicease"), bodyFont));
            lTable.addCell(new Phrase(disease, bodyFont));

            lTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.purpose"), bodyFont));
            lTable.addCell(new Phrase(purpose, bodyFont));

            sb = new StringBuilder();
            sb.append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.pastIllness")).append("\n").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.familyHistory"));
            lTable.addCell(new Phrase(sb.toString(), bodyFont));
            cell = new Cell(new Phrase(pastFamily, bodyFont));
            lTable.addCell(cell);

            sb = new StringBuilder();
            sb.append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.presentIllness")).append("\n").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.testResult")).append("\n").append(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.progressNote"));
            lTable.addCell(new Phrase(sb.toString(), bodyFont));
            lTable.addCell(new Phrase(clinicalCourse, bodyFont));

            lTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.presentMedication"), bodyFont));
            lTable.addCell(new Phrase(medication, bodyFont));

            lTable.addCell(new Phrase(ClientContext.getMyBundle(LetterPDFMaker.class).getString("text.remarks"), bodyFont));
            lTable.addCell(new Phrase(remarks, bodyFont));

            document.add(lTable);

            document.close();
            
            return getPathToPDF();

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
            throw new RuntimeException(ERROR_IO);
        } catch (DocumentException ex) {
            java.util.logging.Logger.getLogger(this.getClass().getName()).warning(ex.getMessage());
            throw new RuntimeException(ERROR_PDF);
        }
    }
}

























