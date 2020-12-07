package miniplc0java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.StackVar;
import miniplc0java.analyser.func;
import miniplc0java.error.CompileError;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {
    public static void main(String[] args) throws CompileError {
        var argparse = buildArgparse();
        Namespace result;
        try {
            result = argparse.parseArgs(args);
        } catch (ArgumentParserException e1) {
            argparse.handleError(e1);
            return;
        }

        var inputFileName = result.getString("input");
        var outputFileName = result.getString("output");

        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        PrintStream output;
        if (outputFileName.equals("-")) {
            output = System.out;
        } else {
            try {
                output = new PrintStream(new FileOutputStream(outputFileName));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open output file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        if (result.getBoolean("tokenize")) {
            // tokenize
            var tokens = new ArrayList<Token>();
            try {
                while (true) {
                    var token = tokenizer.nextToken();
                    if (token.getTokenType().equals(TokenType.EOF)) {
                        break;
                    }
                    if (token.getTokenType().equals(TokenType.ANNOTATION))
                        continue;
                    tokens.add(token);
                }
            } catch (Exception e) {
                // 遇到错误不输出，直接退出
                System.err.println(e);
                System.exit(0);
                return;
            }
            for (Token token : tokens) {
                output.println(token.toString());
            }
        } else if (result.getBoolean("analyse")) {
            // analyze
            var analyzer = new Analyser(tokenizer);
            List<Instruction> instructions;
            try {
                instructions = analyzer.analyse();
            } catch (Exception e) {
                // 遇到错误不输出，直接退出
                System.err.println(e);
                System.exit(0);
                return;
            }
            //----------------------------------
            output.println("72303b3e");
            output.println("00000001");
            output.println(String.format("%08x", analyzer.stack_top1 + 1));
            for (int i = 0; i <= analyzer.stack_top1; i++) {//输出全局变量
                StackVar tmp = analyzer.stack_vars[i];
                if (tmp.isConstant())
                    output.println("01");
                else {
                    output.println("00");
                }
                if (tmp.isIs_fn()) {
                    String name = tmp.getName();
                    output.println(String.format("%08x", name.length()));
                    for (int j = 0; j < name.length(); j++) {
                        StringBuilder str = new StringBuilder();
                        str.append('\'').append(name.charAt(j)).append('\'');
                        output.print(str);
                    }
                    output.print('\n');
                } else {
                    output.println("00000008");
                    output.println("0000000000000000");
                }
            }
            output.println(String.format("%08x", analyzer.func_top + 1));
            for (int i = 0; i <= analyzer.func_top; i++) {
                func tmp = analyzer.func_list[i];
                output.println(String.format("%08x", tmp.global_num));
                if(tmp.return_num!=3){
                    output.println(String.format("%08x", 1));
                }else{
                    output.println(String.format("%08x", 0));
                }
                output.println(String.format("%08x", tmp.args_num));
                output.println(String.format("%08x", tmp.locals_num));
                output.println(String.format("%08x", tmp.getOperations().size()));
//                output.println("("+tmp.func_num+")");
//                StringBuilder str=new StringBuilder().append("fn [").append(tmp.global_num).append("] ")
//                        .append(tmp.locals_num).append(" ").append(tmp.args_num).append(" -> ");
//                if(tmp.return_num!=3){
//                    str.append(1);
//                }else{
//                    str.append(0);
//                }
//                output.println(str+" {");
                ArrayList<Instruction> ops=tmp.getOperations();
                for(int j=0;j<ops.size();j++){
                    output.println(ops.get(j));
                }
//                output.println("}");
            }
        } else {
            System.err.println("Please specify either '--analyse' or '--tokenize'.");
            System.exit(3);
        }
    }

    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("miniplc0-java");
        var parser = builder.build();
        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
