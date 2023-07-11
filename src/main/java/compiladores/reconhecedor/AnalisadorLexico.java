package compiladores.reconhecedor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class AnalisadorLexico {
    private String caminhoArquivo;
    private String nomeArquivo;
    private int c;
    PushbackReader br;
    BufferedReader InitialReader;



    private ArrayList<String> reservedWords = new ArrayList<String>(Arrays.asList(
            "and", "array", "begin", "case", "const", "div",
            "do", "downto", "else", "end", "file", "for",
            "function", "goto", "if", "in", "label", "mod",
            "nil", "not", "of", "or", "packed", "procedure",
            "program", "record", "repeat", "set", "then",
            "to", "type", "until", "var", "while", "with",
            "read", "write", "real", "integer"
    ));

    public AnalisadorLexico(String nomeArquivo) {
        this.caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        this.nomeArquivo = nomeArquivo;

        try {
            this.InitialReader = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            this.br = new PushbackReader(InitialReader);
            this.c = this.br.read();
        } catch (IOException err) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + this.nomeArquivo);
            err.printStackTrace();
        }
    }


    public Token getToken(int linha, int coluna) {
        int tamanhodoToken = 0;
        int qtdEspacos = 0;
        int e;
        StringBuilder lexema = new StringBuilder("");
        char caractere;
        Token token = new Token();

        try {

            while (c != -1) {
                caractere = (char) c;
                if (!(c == 13 || c == 10)) {
                    if (caractere == ' ') {
                        while (caractere == ' ') {
                            c = this.br.read();
                            qtdEspacos++;
                            caractere = (char) c;
                        }
                    } else if (Character.isLetter(caractere)) {
                        while (Character.isLetter(caractere) || Character.isDigit(caractere)) {
                            lexema.append(caractere);
                            c = this.br.read();
                            tamanhodoToken++;
                            caractere = (char) c;
                        }

                        if(IsReservedWord(lexema.toString())){
                            token.setClasse(Classe.cPalRes);
                        } else {
                            token.setClasse(Classe.cId);
                        }
                        SetColumnRowSize(token,coluna,linha,tamanhodoToken,qtdEspacos);
                        Valor valor = new Valor(lexema.toString());
                        token.setValor(valor);
                        return token;
                    } else if (Character.isDigit(caractere)) {
                        int numberPoints = 0;
                        while (Character.isDigit(caractere) || caractere=='.') {
                            if(caractere=='.'){
                                numberPoints++;
                            }
                            lexema.append(caractere);
                            c = this.br.read();
                            tamanhodoToken++;
                            caractere = (char) c;
                        }
                        if(numberPoints<=1){
                            IntegerAndReal(token, lexema, numberPoints);
                            SetColumnRowSize(token,coluna,linha,tamanhodoToken,qtdEspacos);
                            return token;
                        }
                    } else {
                        tamanhodoToken++;
                        if(caractere==':' | caractere=='>' | caractere=='<'){
                            tamanhodoToken=OperatorsNextRetroced(token,caractere,tamanhodoToken);
                        }
                        else {
                            OperatorsSimples(token,caractere);
                        }
                        SetColumnRowSize(token,coluna,linha,tamanhodoToken,qtdEspacos);
                        token.setValor(null);
                        c = this.br.read();
                        tamanhodoToken++;
                        return token;
                    }
                }else{
                    c = this.br.read();
                    linha++;
                    qtdEspacos=0;
                    tamanhodoToken=0;
                    coluna=1;
                }
            }

            token.setClasse(Classe.cEOF);
            return token;
        } catch (IOException err) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + this.nomeArquivo);
            err.printStackTrace();
        }
        return null;
    }


    private void SetColumnRowSize(Token token,int coluna,int linha,int tamanhodoToken,int qtdEspacos )
    {
        token.setTamanhoToken(tamanhodoToken);
        token.setColuna(coluna+qtdEspacos);
        token.setLinha(linha);
    }


    private void IntegerAndReal(Token token, StringBuilder lexema, int numberPoints) {
        if (numberPoints == 0) {
            token.setClasse(Classe.cInt);
            Valor valor = new Valor(Integer.parseInt(lexema.toString()));
            token.setValor(valor);
        } else {
            token.setClasse(Classe.cReal);
            Valor valor = new Valor(Float.parseFloat(lexema.toString()));
            token.setValor(valor);
        }
    }

    public int OperatorsNextRetroced(Token token,char caractere,int tamanhodoToken) throws IOException {
        int tamanhodoTokenF=tamanhodoToken;
        if(caractere==':'){
            int proximo = this.br.read();
            caractere = (char) proximo;
            if(caractere=='='){
                tamanhodoTokenF++;
                token.setClasse(Classe.cAtribuicao);
            }else{
                this.br.unread(proximo);
                token.setClasse(Classe.cDoisPontos);
            }
        }else if(caractere=='>'){
            int proximo = this.br.read();
            caractere = (char) proximo;
            if(caractere=='='){
                tamanhodoTokenF++;
                token.setClasse(Classe.cMaiorIgual);
            }else{
                this.br.unread(proximo);
                token.setClasse(Classe.cMaior);
            }
        }else if(caractere=='<'){
            int proximo = this.br.read();
            caractere = (char) proximo;
            if(caractere=='='){
                tamanhodoTokenF++;
                token.setClasse(Classe.cMenorIgual);
            }else if(caractere=='>'){
                tamanhodoTokenF++;
                token.setClasse(Classe.cDiferente);
            }else{
                this.br.unread(proximo);
                token.setClasse(Classe.cMenor);
            }
        }
        return tamanhodoTokenF;
    }

    public void OperatorsSimples(Token token,char caractere)
    {
        if(caractere=='+'){
            token.setClasse(Classe.cMais);
        }else if(caractere=='-'){
            token.setClasse(Classe.cMenos);
        }else if(caractere=='/'){
            token.setClasse(Classe.cDivisao);
        }else if(caractere=='*'){
            token.setClasse(Classe.cMultiplicacao);
        }else if(caractere=='='){
            token.setClasse(Classe.cIgual);
        }else if(caractere==','){
            token.setClasse(Classe.cVirgula);
        }else if(caractere==';'){
            token.setClasse(Classe.cPontoVirgula);
        }else if(caractere=='.'){
            token.setClasse(Classe.cPonto);
        }else if(caractere=='('){
            token.setClasse(Classe.cParEsq);
        }else if(caractere==')'){
            token.setClasse(Classe.cParDir);
        }else{
            token.setClasse(Classe.cEOF);
        }
    }

    boolean IsReservedWord(String word){
        return this.reservedWords.contains(word.toLowerCase());
    }
}
