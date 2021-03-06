import java.util.ArrayList;
import java.util.List;

enum CodeSmellType {LongMethod, ComentáriosNoMeioDoCodigo, WhileTrue, NomeFicheiroErrado, NomeClasseLetraMinuscula,
                    VariaveisPrivadas, VariaveisUmCaracterForaMetodo, VariaveisUmCaracter, InputOutputGenerico,Excessao,
                    NoToString, NoClone, NoEquals, LargeClass, ManyFinals, UsoHeranca};

public class CodeSmell {
    //TODO colocar os restantes!!!
    CodeSmellType codeSmell;
    List<Integer> linhas;   //  linhas onde ocorre

    public CodeSmell(){
        this.linhas = new ArrayList<>();
    }

    public CodeSmell(CodeSmellType cst, int linha){
        this.codeSmell = cst;
        this.linhas = new ArrayList<>();
        this.linhas.add(linha);
    }

    @Override
    public String toString() {
        return "CodeSmell{" +
                "codeSmell=" + codeSmell +
                ", linhas=" + linhas +
                '}';
    }
}
