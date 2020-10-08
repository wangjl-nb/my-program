#include<stdio.h>
#include<stdlib.h>
#include<stdbool.h>
#include<ctype.h>
#include<string.h>
// int BEGINSY =1,ENDSY=2,FORSY=3,DOSY=4,IFSY=5,THENSY=6\
// ,ELSESY=7,IDSY=8,INTSY=9,COLONSY=10,PLUSSY=11,STARSY=12,COMSY=13\
// ,LPARSY=14,RPARSY=15,ASSIGNSY=16;
char ch,token[100]=""; 
char string[][10]={"BEGIN","END","FOR","IF","THEN","ELSE"};
char string1[][10] = {"Begin", "End", "For", "If", "Then", "Else"};
int point=0;
int is_reserved(char ch[]){
    for(int i=0;i<sizeof(string[0]);i++){
        if(strcmp(ch,string[i])==0){
            return i+1;
        }
    }
    return 0;
}
bool is_space(char c){
    if(c==0x20||c==0x09||c==0x0a||c==0x0b||c==0x0c||c==0x0d){
        return true;//空白字符（空格，水平制表符，换行符，垂直制表符，换页，回车）
    }
    else{
        return false;
    }
}
bool is_letter(char c){//判断是否是字母
    if((c<='z'&&c>='a')||(c>='A'&&c<='Z')){
        return true;
    }else{
        return false;
    }
}
bool is_digit(char c){//判断是否是数字
    if(c<='9'&&c>='0'){
        return true;
    }
    else{
        return false;
    }
}
char * delete_zeros(char ch[]){
    char res[100]="";
    int i=0;
    while(ch[i]=='0')i++;
    return &ch[i];
}
int turn_to_num(char ch[]){
    int num=0;
    for(int i=0;i<strlen(ch);i++){
        num=num*10+ch[i]-'0';
    }
    return num;
}
int main(int argc, char const *argv[])
{
    FILE * fp =fopen(argv[1],"r");
    if(fp==NULL){
        printf("文件打开失败");
        return 0;
    }
    while((ch=fgetc(fp))!=EOF){
        if(is_space(ch)){//判断是否是空白字符
            continue;
        }
        memset(token,0,sizeof(token));//清空字符数组
        point=0;
        if(is_letter(ch)){
            token[point++] = ch;
            while ((ch = fgetc(fp)) != EOF){
                if(is_letter(ch)||is_digit(ch)){
                    token[point++] = ch;
                }else{
                    ungetc(ch,fp);
                    break;
                }
            }
            int c=is_reserved(token);
            if(c==0){
                printf("Ident(%s)\n", token);
            }else{
                printf("%s\n", string1[c - 1]);
            }
        }
        else if(is_digit(ch)){
            token[point++] = ch;
            while ((ch = fgetc(fp)) != EOF)
            {
                if (is_digit(ch)){
                    token[point++] = ch;
                }
                else{
                    ungetc(ch, fp);
                    break;
                }
            }
            printf("Int(%d)\n", turn_to_num(delete_zeros(token)));
        }else if(ch=='+'){
            printf("Plus\n");
        }
        else if (ch == '*'){
            printf("Star\n");
        }else if(ch == ','){
            printf("Comma\n");
        }else if(ch == '('){
            printf("LParenthesis\n");
        }else if(ch == ')'){
            printf("RParenthesis\n");
        }else if(ch == ':'){
            ch=fgetc(fp);
            if(ch=='='){
                printf("Assign\n");
            }else{
                ungetc(ch,fp);
                printf("Colon\n");
            }
        }else{
            printf("Unknown\n");
            break;
        }
    }
    fclose(fp);
    return 0;
}
