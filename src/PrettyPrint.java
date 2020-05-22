import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrettyPrint {

    public static String importCss = "<head><link rel=\"stylesheet\" href=\"..\\styleSheets\\style.css\"></head>";

    //  MÉTODOS GENÉRICOS #############################################################################################
    //  ###############################################################################################################

    /**
     * Converte um Map do tipo <String, Integer> para um tabela HTML
     * @param data dados (Map)
     * @param column1 nome da coluna de 1 da tabela
     * @param column2 nome da coluna de 2 da tabela
     * @returnm retorna a string HTML da tabela
     */
    private static String convertMapStringIntegerToHTMLTable(Map<String, Integer> data, String column1, String column2){
        String result = "<table>\n" +
                "  <tr>\n" +
                "    <th>" + column1 + "</th>\n" +
                "    <th>" + column2 + "</th>\n" +
                "  </tr>\n";
        List<Map.Entry<String, Integer>> list = new ArrayList<>(data.entrySet());
        list.sort(Map.Entry.comparingByValue());
        for (Map.Entry<String, Integer> entry : list) {
            result += "  <tr>\n" +
                    "    <td>" + entry.getKey() + "</td>\n" +
                    "    <td>" + entry.getValue() + "</td>\n" +
                    "  </tr>\n";
        }
        result += "</table>";
        return result;
    }

    /**
     * Gera o ínicio de um ficheiro HTML.
     * @param fw file writer do ficheiro
     * @param title título da página
     * @throws IOException
     */
    public static void headerHTML(FileWriter fw, String title) throws IOException {
        fw.write("<html>" +importCss);
        fw.write("<body>" +
                "   <h2>" + title + "</h2>");
    }

    /**
     * Gera o fim de uma página html
     * @param fw file writer do ficheiro
     * @throws IOException
     */
    public static void footerHTML(FileWriter fw) throws IOException {
        fw.write("    </body>" +
                "</html>");
        fw.close();
    }

    //  MÉTODOS GENÉRICOS #############################################################################################
    //  ###############################################################################################################

    //  LONG METHOD ---------------------------------------------------------------------------------

    public static void LongMethod(Ficheiro ficheiro)throws Exception{
        String newFileName = ficheiro.fileName.split("\\.")[0];
        FileWriter fw = new FileWriter(GProject.output +newFileName+"LongMethod.html");

        headerHTML(fw, "Long Method");

        if(ficheiro.methods.size() != 0)
            fw.write(printTableLongMethod(ficheiro.methods,"Método","Linhas"));
        else
            fw.write("Não foram encontrados problemas com esta norma!");

        footerHTML(fw);

        fw.close();
    }

    private static String printTableLongMethod(Map<String,Method> map,String col1,String col2) {
        String result = "<table>\n" +
                "  <tr>\n" +
                "    <th>" + col1 + "</th>\n" +
                "    <th>" + col2 + "</th>\n" +
                "  </tr>\n";
        for (Map.Entry<String, Method> entry : map.entrySet()) {
            for (CodeSmell codeSmell : entry.getValue().codeSmells) {
                if (codeSmell.codeSmell.equals(CodeSmellType.LongMethod)) {
                    result += "  <tr>\n" +
                            "    <td>" + entry.getKey() + "</td>\n" +
                            "    <td>" + codeSmell.linhas + "</td>\n" +
                            "  </tr>\n";
                }
            }
        }
        result += "</table>";
        return result;
    }

    // LONG METHOD ---------------------------------------------------------------------------------

    //  TIPOS PRIMITIVOS ---------------------------------------------------------------------------

    public static void tiposPrimitivos(Ficheiro ficheiro) throws IOException {
        String newFileName = ficheiro.fileName.split("\\.")[0];
        FileWriter fw = new FileWriter(GProject.output +newFileName+"TiposPrimitivos.html");
        headerHTML(fw, "Tipos Primitivos");
        if(ficheiro.usoVariaveisPrimitivas.size() != 0)
            fw.write(convertMapStringIntegerToHTMLTable(ficheiro.usoVariaveisPrimitivas,"Nome da variável ou função","Linha"));
        else
            fw.write("Não foram encontrados problemas com esta norma!");
        footerHTML(fw);
        fw.close();
    }

    //  TIPOS PRIMITIVOS ---------------------------------------------------------------------------
}