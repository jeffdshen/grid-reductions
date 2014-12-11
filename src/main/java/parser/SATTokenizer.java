package parser;


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
public class SATTokenizer {

    private String expression;
    private int idx;
    private String nextToken;
    private boolean ended;

    public SATTokenizer(String expression) throws Exception{
        this.expression = expression;
        this.idx = 0;
        this.loadNextToken();
        this.ended = false;
    }

    // print out all tokens
    public void test() throws Exception{
        while(hasMoreTokens()){
            System.out.println(getNextToken());
        }
    }

    public void reset() throws Exception{
        this.idx = 0;
        this.loadNextToken();
        ended = false;
    }

    //Done weirdly to skip whitespace at the end and terminate properly
    public String getNextToken() throws Exception{
        String token = nextToken;
        this.loadNextToken();
        return token;
    }

    public boolean hasMoreTokens(){
        return !ended;
    }
    public boolean hasMoreChars() { return idx < expression.length(); }

    //tokens: ( ) ! && || alphanumeric. true and false are reserved keywords
    // saves next token into nextToken. Increases idx appropriately
    private void loadNextToken() throws Exception{
        if(hasMoreChars()){
            char currentChar = expression.charAt(idx);
            idx ++;

            switch (currentChar) {
                //skip whitespace
                case ' ':
                case '\n':
                case '\t':
                case '\r':
                    loadNextToken();
                    return;
                case '(':
                    nextToken = "(";
                    return;
                case ')':
                    nextToken = ")";
                    return;
                case '!':
                    nextToken = "!";
                    return;
                case '&':
                    if(hasMoreChars() && expression.charAt(idx) == '&'){
                        idx ++;
                        nextToken = "&&";
                        return;
                    }
                    else{
                        throw new Exception("Parser error: Expected & at input index " + idx + ". Got " + currentChar);
                    }
                case '|':
                    if(hasMoreChars() && expression.charAt(idx) == '|'){
                        idx ++;
                        nextToken = "||";
                        return;
                    }
                    else{
                        throw new Exception("Parser error: Expected | at input index " + idx + ". Got " + currentChar);
                    }
                default:
                    if(Character.isLetterOrDigit(currentChar)){
                        StringBuilder str = new StringBuilder();
                        str.append(currentChar);
                        if(!hasMoreChars()){
                            nextToken = str.toString();
                            return;
                        }
                        currentChar = expression.charAt(idx);
                        while(Character.isLetterOrDigit(currentChar)){

                            str.append(currentChar);
                            idx ++;
                            if(hasMoreChars()) {
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
        else{
            ended = true;
        }
    }

}