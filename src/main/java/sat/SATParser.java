package sat;


/**
 * Parser for converting boolean expressions in CNF to a configuartion
 *
 * Ignores whitespace
 *
 * Expression should have form
 *   E := E && (V || V || V)
 *   V := [a-zA-Z1-9]+
 *
 *
 */
public class SATParser {

    private String expression;
    private int idx;
    private String nextToken;

    public SATParser(String expression){
        this.expression = expression;
        this.idx = 0;
        this.loadNextToken();
    }

    // print out all tokens
    public void test(){
        while(hasMoreTokens()){
            System.out.println(getNextToken());
        }
    }

    public void reset(){
        this.idx = 0;
        this.loadNextToken();
    }

    //Done weirdly to skip whitespace at the end and terminate properly
    public String getNextToken(){
        String token = nextToken;
        this.loadNextToken();
        return token;
    }

    public boolean hasMoreTokens(){
        return idx < expression.length();
    }

    // saves next token into nextToken. Increases idx appropriately
    private void loadNextToken(){
        if hasMoreTokens() {
            char currentChar = expression.charAt(idx);
            idx ++;

            switch (currentChar) {
                //skip whitespace
                case ' ':
                case '\n':
                case '\t':
                case '\r':
                    loadNextToken()
                case '(':
                    nextToken = "(";
                    return;
                case ')':
                    nextToken = ")";
                    return;
                case '&':
                    if(expression.charAt(idx) == '&'){
                        idx ++;
                        nextToken = "&&";
                        return
                    }
                    else{
                        throw new Exception("Parser error: Expected & at input index " + idx + ". Got " + currentChar);
                    }
                case '|':
                    if(expression.charAt(idx) == '|'){
                        idx ++;
                        nextToken = "||";
                        return;
                    }
                    else{
                        throw new Exception("Parser error: Expected | at input index " + idx + ". Got " + currentChar);
                    }
                default:
                    if(Character.isLetterOrDigit(currentChar)){
                        StringBuilder str = new StringBuilder(currentChar);

                        currentChar = expression.charAt(idx);
                        while(Character.isLetterOrDigit(currentChar)){
                            str.append(currentChar);
                            idx ++;
                            if(hasMoreTokens()) {
                                currentChar = expression.charAt(idx);
                            }
                            else{
                                break;
                            }
                        }
                        nextToken = str.toString();
                        return;
                    }
                    else{
                        throw new Exception("Parser error: Encountered illegal char " + currentChar + " at index " + idx);
                    }
            }
        }
    }

}