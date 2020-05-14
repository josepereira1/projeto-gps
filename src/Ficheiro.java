import java.util.*;

public class Ficheiro {

    //  TODO pensar melhor como vamos guardar isto dos métodos
    //static Map<String, Integer > methods;    //  Ficheiro->tostring()
    String fileName;
    String className;
    String[] linhas;
    int numeroLinhas = 0;
    //  apenas codesemells relacionados com a classe
    List<CodeSmell> codeSmells;
    boolean toString;
    boolean equals;
    boolean clone;
    boolean construtoVazio;
    boolean constutorParametrizado;
    boolean construtorCopia;

    // a flag de analisar o método está a ser usada
    boolean insideMethod;
    boolean identifyPrimitives;
    boolean insideComment = false;

    //  chave: nome da variável
    Map<String, Integer> variaveisNaoPrivadas;

    //  chave: Nome do método - temos a info do número de linhas, e code smells
    Map<String, Method> methods;

    Map<String, Integer> usoVariaveisPrimitivas;

    List<String> dependencias;  //  dependências de classes;

    List<Integer> linhasDeComentarios;

    // linha atual do código
    int linhaAtual = 1;

    // nº de linhas atuais do método
    int linhasMetodo;

    // chavetas abertas no método
    int chavetasAbrir = 1;

    // chavetas fechadas no método
    int chavetasFechar = 0;

    // nome do método atual
    String nomeMetodo;

    // contador de variáveis final
    int finalCounter = 0;

    final String nomeMetodoPadrao = "(public|protected|private|static)(\\ |\\t)+(?!class)[A-Za-z<>]+(\\ |\\t)+[A-Za-z]+(\\ |\\t)*(\\ |\\(.*\\{)";
    final String chavetasPadrao = "[\\{\\}]";
    final String whileTruePadrao = "while\\(true\\)\\{";
    final String excecaoPadrao ="throws";
    final String inputOutputPadrao = "(ArrayList|List|HashMap|Set|Queue|Dequeue|Map|ListIterator|SortedSet|SortedMap|HashSet|TreeSet|LinkedList|TreeMap|PriorityQueue)";
    final int numeroMaximoLinhas = 10;
    final String classNamePadrao1 = "\\s*(public|private)\\s+class\\s+(\\w+)\\s+((extends\\s+\\w+)|(implements\\s+\\w+( ,\\w+)*))?\\s*\\{";
    final String classNamePadrao2 = "\\s*(public|private)\\s+class\\s+(\\w+)";
    final String variaveisPrivadasPadrao = "private[A-Za-z0-9 <>,\\[\\]]+[=;]";
    final String variaveisUmCarater = "(final)?[A-Za-z\\[\\]<>, ]+ +[A-Za-z] *[;=]";
    final String simpleComments = "\\/\\/";

    final String variaveisComTiposPrimitivos = "(byte|short|int|long|float|double|char|boolean){1}[\\[\\]]*(\\ |\\t)+[?:A-Za-z0-9]+";

    final String toStringPadrao = "public[\\ \\t]+String[\\ \\t]+toString[\\ \\t]*\\([\\ \\t]*\\)[\\ \\t]*\\{";
    String clonePadrao; // definido depois de encontrada a classname
    String equalsPadrao; // definido depois de encontrada a classname
    final String finalPadrao = "final[\\ \\t]+";
    final int MAX_FINAL = 5; // + que 5 variáveis final é code smell
    final int MAX_LINES = 200; // + que 200 linhas é considerada large class
    final int MAX_METHODS = 10; // + que 10 métodos é considerada large class


    public Ficheiro() {
        this.codeSmells = new ArrayList<>();
        this.variaveisNaoPrivadas = new HashMap<>();
        this.methods = new HashMap<>();
        this.usoVariaveisPrimitivas = new HashMap<>();
        this.dependencias = new ArrayList<>();
        this.linhasDeComentarios = new ArrayList<>();
    }


    public Ficheiro(String fileName) {
        this.fileName = fileName;
        this.codeSmells = new ArrayList<>();
        this.variaveisNaoPrivadas = new HashMap<>();
        this.methods = new HashMap<>();
        this.usoVariaveisPrimitivas = new HashMap<>();
        this.dependencias = new ArrayList<>();
        this.linhasDeComentarios = new ArrayList<>();
    }

    public Ficheiro(String className, String[] linhas, int numeroLinhas, List<CodeSmell> codeSmells, boolean toString, boolean equals, boolean clone, boolean construtoVazio, boolean constutorParametrizado, boolean construtorCopia, Map<String, Integer> variaveisNaoPrivadas, Map<String, Method> methods, Map<String, Integer> usoVariaveisPrimitivas, List<String> dependencias, List<Integer> linhasDeComentarios) {
        this.className = className;
        this.linhas = linhas;
        this.numeroLinhas = numeroLinhas;
        this.codeSmells = codeSmells;
        this.toString = toString;
        this.equals = equals;
        this.clone = clone;
        this.construtoVazio = construtoVazio;
        this.constutorParametrizado = constutorParametrizado;
        this.construtorCopia = construtorCopia;
        this.variaveisNaoPrivadas = variaveisNaoPrivadas;
        this.methods = methods;
        this.usoVariaveisPrimitivas = usoVariaveisPrimitivas;
        this.dependencias = dependencias;
        this.linhasDeComentarios = linhasDeComentarios;
    }

    public void run() throws Exception{
        int i = 1;

        while(!checkClassName(linhas[i++]));
        clonePadrao = "public[\\ \\t]+" + className + "[\\ \\t]+clone[\\ \\t]*\\([\\ \\t]*" + className + "[\\ \\t]+.*[\\ \\t]*\\)[\\ \\t]*\\{";
        equalsPadrao = "public[\\ \\t]+boolean[\\ \\t]+equals[\\ \\t]*\\([\\ \\t]*" + className + "[\\ \\t]+.*[\\ \\t]*\\)[\\ \\t]*\\{";

        for (; i <= linhas.length; linhaAtual = ++i) {
            String linha = linhas[i - 1];
            checkFimComentario(linha);  //  tem que se verificar antes do if
            if(!insideComment) { //  quando estamos dentro de comentários, não vale apena verificar nenhum code smell
                checkFinalVariables(linha);
                checkTiposPrimitivos(linha);
                checkComentariosSimples(linha); //  TODO tou a checkar os comentários fora dos métodos tb, não se era suposto, depois decide-se
                if (insideMethod) {
                    checkToStringEqualsOrClone(linha);
                    checkVariaveisUmCaracter(linha);
                    checkWhileTrue(linha);
                    checkFimMehtod(linha);
                    checkInicioComentario(linha);
                    //  é necessário colocar aqui este método (checkFimComentario), para os casos de comentários /* */ na mesma linha, pq apesar de checkar antes do if
                    //  , quando passar no checkInicioComentario, vai por o insideComment a true, e nunca mais entrava neste if se não checkasse o fim
                    checkFimComentario(linha);
                } else {
                    checkVariaveisPrivadas(linha);
                    checkInicioMethod(linha);
                }
            }
           //System.err.println("RUN : " + linhas[i-1]);
            //checkVariaveis();
        }
        //System.out.println(methods);
        if (finalCounter > MAX_FINAL) {
            CodeSmell cs = new CodeSmell(CodeSmellType.ManyFinals, -1);
            this.codeSmells.add(cs);
        }
        if (linhas.length > MAX_LINES || methods.size() > MAX_METHODS) {
            CodeSmell cs = new CodeSmell(CodeSmellType.LargeClass, -1);
            this.codeSmells.add(cs);
        }
        if (toString == false) {
            CodeSmell cs = new CodeSmell(CodeSmellType.NoToString, -1);
            this.codeSmells.add(cs);
        }
        if (equals == false) {
            CodeSmell cs = new CodeSmell(CodeSmellType.NoEquals, -1);
            this.codeSmells.add(cs);
        }
        if (clone == false) {
            CodeSmell cs = new CodeSmell(CodeSmellType.NoClone, -1);
            this.codeSmells.add(cs);
        }
        /*
        System.out.println(fileName);
        System.err.println("toString=" + toString);
        System.err.println("equals=" + equals);
        System.err.println("clone=" + clone);
        System.err.println("finalCounter=" + finalCounter);
        System.out.println(codeSmells);
        */
    }

    private void checkFinalVariables(String linha) {
        List<String> l = RegularExpression.findAll(linha, finalPadrao);
        if (l.isEmpty() == false) finalCounter++;
    }

    public void checkToStringEqualsOrClone(String linha) {
        List<String> l = RegularExpression.findAll(linha, toStringPadrao);
        if (l.isEmpty() == false) { toString = true; return; }
        l = RegularExpression.findAll(linha, equalsPadrao);
        if (l.isEmpty() == false) { equals = true; return; }
        l = RegularExpression.findAll(linha, clonePadrao);
        if (l.isEmpty() == false) { clone = true; return; }
    }


    public void checkInicioMethod(String line) throws Exception{
        String pattern = nomeMetodoPadrao;
        List<String> l = RegularExpression.findAll(line, pattern);

        if(l.size() != 0) {
            //System.out.println(l.get(0));
            linhasMetodo = 0;
            chavetasAbrir = 1;
            insideMethod = true;
            nomeMetodo = l.get(0);
            Method m = new Method(0, new ArrayList<>());
            methods.put(nomeMetodo, m);
            checkInputOutputGenerico(line);
            checkExcessao(line);
        }
    }
    /*
    public boolean checkVariaveis(){

    }*/

    public void checkFimMehtod(String line) {
        linhasMetodo++;
        String pattern = chavetasPadrao;
        List<String> l = RegularExpression.findAll(line, pattern);
        //System.out.println(line);
        for(String s : l){
            //System.out.println("f" + s);
            if (s.equals("{")) chavetasAbrir++;
            else if(s.equals("}")) chavetasFechar++;
        }
        if (chavetasAbrir == chavetasFechar){
            //System.out.println(this.methods.size());
            this.methods.get(nomeMetodo).linhas = linhasMetodo;
            if(linhasMetodo > numeroMaximoLinhas){
                CodeSmell cs = new CodeSmell(CodeSmellType.LongMethod, linhaAtual-linhasMetodo);
                methods.get(nomeMetodo).codeSmells.add(cs);
            }
            insideMethod = false;
            chavetasFechar = chavetasAbrir = 0;
        }
    }

    public void checkWhileTrue(String line){
        String pattern = whileTruePadrao;
        List<String> l = RegularExpression.findAll(line, pattern);

        if(l.size() != 0){
            Method method = methods.get(nomeMetodo);
            CodeSmell cs = new CodeSmell(CodeSmellType.WhileTrue, linhaAtual);
            method.codeSmells.add(cs);
        }
    }

    public void checkInputOutputGenerico(String line){

        String pattern = inputOutputPadrao;
        List<String> l = RegularExpression.findAll(line, pattern);

        if(l.size() != 0){
            //System.out.println("INPUT/OUTPUT Não Generico: "+ line);
            Method method = methods.get(nomeMetodo);
            CodeSmell cs = new CodeSmell(CodeSmellType.InputOutputGenerico, linhaAtual);
            method.codeSmells.add(cs);
        }
    }

    public void checkExcessao(String line) throws Exception{
        String pattern = excecaoPadrao;
        List<String> l = RegularExpression.findAll(line, pattern);

        //System.out.println(line);
        if(l.size() == 0){
         //   System.out.println(" Metodo sem Excessao: "+ line);
            Method method = methods.get(nomeMetodo);
            CodeSmell cs = new CodeSmell(CodeSmellType.Excessao, linhaAtual);
            method.codeSmells.add(cs);
        }
    }

    public void checkVariaveisPrivadas(String line){
        List<String> l = RegularExpression.findAll(line, variaveisPrivadasPadrao);

        if(l.size() != 0){
            CodeSmell cs = new CodeSmell(CodeSmellType.VariaveisPrivadas, linhaAtual);
            this.codeSmells.add(cs);
        }
    }

    public void checkVariaveisUmCaracter(String line){
        List<String> l = RegularExpression.findAll(line, variaveisUmCarater);

        if(l.size() != 0){
            Method method = methods.get(nomeMetodo);
            CodeSmell cs = new CodeSmell(CodeSmellType.VariaveisUmCaracter, linhaAtual);
            method.codeSmells.add(cs);
        }
    }

    public boolean checkClassName(String line){
        List<String> l = RegularExpression.findAll(line, classNamePadrao1);

        if(l.size() != 0){
            String c = l.get(0);
            if (c.contains("extends")) { /*System.out.println("Classe que usa herança");*/ }
            if (c.contains("implements")) { /*System.out.println("Classe que implementa interfaces");*/ }

            List<String> lAux = RegularExpression.findAll(c, classNamePadrao2);
            String auxName = lAux.get(0).replace(" ", "");
            if(auxName.contains("private")) className = auxName.substring(12);
            else className = auxName.substring(11);

            //System.out.println("FILE NAME : " + fileName);
            //System.out.println("CLASS NAME : " + className);

            if(!className.equals(fileName.substring(0, fileName.length() - 5))){
                CodeSmell codeSmell = new CodeSmell();
                codeSmell.codeSmell = CodeSmellType.NomeFicheiroErrado;
                codeSmell.linhas.add(linhaAtual);
            }
            if(Character.isUpperCase(className.charAt(0))){
                CodeSmell codeSmell = new CodeSmell();
                codeSmell.codeSmell = CodeSmellType.NomeClasseLetraMinuscula;
                codeSmell.linhas.add(linhaAtual);
            }

            return true;
        }
        return false;
    }

    /**
     * Verifica o uso de tipos primitivos em variáveis, retornos e parâmetros de funções.
     * @param line linha a ser processada
     */
    public void checkTiposPrimitivos(String line){
        for(String var : RegularExpression.findAll(line, this.variaveisComTiposPrimitivos)) {
            String[] r = var.split(" ");    //  separar o tipo da variável
            this.usoVariaveisPrimitivas.put(r[r.length-1], linhaAtual); //  r.length-1, pois pode ter mais do que um espaço
        }
    }

    public void checkComentariosSimples(String line){
        if(RegularExpression.findAll(line, this.simpleComments).size() > 0)
            this.linhasDeComentarios.add(linhaAtual);
    }

    public void checkInicioComentario(String line){
        String initComment = "\\/\\*";

        /*
         *
         * exemplo para ser capturado
         *
         * */

        if(RegularExpression.findAll(line, initComment).size() > 0){
            this.insideComment = true;
            this.linhasDeComentarios.add(linhaAtual);
        }
    }

    public void checkFimComentario(String line){
        String endComment = "\\*\\/";
        if(RegularExpression.findAll(line, endComment).size() > 0){
            this.insideComment = false;
        }
    }

    @Override
    public String toString() {
        return "Ficheiro{" +
                "fileName='" + fileName + '\'' +
                ", className='" + className + '\'' +
                ", linhas=" + Arrays.toString(linhas) +
                ", numeroLinhas=" + numeroLinhas +
                ", codeSmells=" + codeSmells +
                ", toString=" + toString +
                ", equals=" + equals +
                ", clone=" + clone +
                ", construtoVazio=" + construtoVazio +
                ", constutorParametrizado=" + constutorParametrizado +
                ", construtorCopia=" + construtorCopia +
                ", insideMethod=" + insideMethod +
                ", identifyPrimitives=" + identifyPrimitives +
                ", variaveisNaoPrivadas=" + variaveisNaoPrivadas +
                ", methods=" + methods +
                ", usoVariaveisPrimitivas=" + usoVariaveisPrimitivas +
                ", dependencias=" + dependencias +
                ", linhaAtual=" + linhaAtual +
                ", linhasMetodo=" + linhasMetodo +
                ", chavetasAbrir=" + chavetasAbrir +
                ", chavetasFechar=" + chavetasFechar +
                ", nomeMetodo='" + nomeMetodo + '\'' +
                ", finalCounter=" + finalCounter +
                ", nomeMetodoPadrao='" + nomeMetodoPadrao + '\'' +
                ", chavetasPadrao='" + chavetasPadrao + '\'' +
                ", whileTruePadrao='" + whileTruePadrao + '\'' +
                ", excecaoPadrao='" + excecaoPadrao + '\'' +
                ", inputOutputPadrao='" + inputOutputPadrao + '\'' +
                ", numeroMaximoLinhas=" + numeroMaximoLinhas +
                ", classNamePadrao1='" + classNamePadrao1 + '\'' +
                ", classNamePadrao2='" + classNamePadrao2 + '\'' +
                ", variaveisPrivadasPadrao='" + variaveisPrivadasPadrao + '\'' +
                ", variaveisUmCarater='" + variaveisUmCarater + '\'' +
                ", toStringPadrao='" + toStringPadrao + '\'' +
                ", clonePadrao='" + clonePadrao + '\'' +
                ", equalsPadrao='" + equalsPadrao + '\'' +
                ", finalPadrao='" + finalPadrao + '\'' +
                ", MAX_FINAL=" + MAX_FINAL +
                ", MAX_LINES=" + MAX_LINES +
                ", MAX_METHODS=" + MAX_METHODS +
                '}';
    }
}