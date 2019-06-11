package poke;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.TreeMap;

class Pokemon {
    StringBuffer downloadList() throws Exception {
        String poke = "https://www.pokewiki.de/Pok%C3%A9mon-Liste";
        URLConnection connection = new URL(poke).openConnection();

        Scanner s = new Scanner(connection.getInputStream());
        boolean start = false;
        StringBuffer sb = new StringBuffer();
        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (line.contains("<tbody>")) {
                start = true;
            }
            if (start) {
                sb.append(line.replaceAll("</table>", "")).append("\n");
                if (line.contains("</tbody>")) {
                    break;
                }
            }
        }
        return sb;
    }

    TreeMap<String, String> buildMap(StringBuffer sb) throws Exception {
        TreeMap<String, String> m = new TreeMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);
        NodeList trs = doc.getElementsByTagName("tr");
        String de = "", en = "";
        for (int i = 0; i < trs.getLength(); i++) {

            NodeList tds = trs.item(i).getChildNodes();
            for (int j = 0; j < tds.getLength(); j++) {
                Node td = tds.item(j);
                boolean isDE = j == 5;
                boolean isEN = j == 7;
                String cont = td.getTextContent();

                if (isDE) {
                    de = cont;
                }
                if (isEN && !"".equalsIgnoreCase(de)) {
                    en = cont;
                    m.put(de, en);
                }
                if (isDE || isEN) {
                    //   System.out.println((isDE ? "DE" : "EN") + ":" + cont);
                }

            }
        }
        return m;
    }
}
