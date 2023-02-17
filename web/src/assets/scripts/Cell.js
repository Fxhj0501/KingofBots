export class Cell{
    constructor(r,c){
        this.r = r;
        this.c = c;
        //蛇头和身体是圆，因此设计x,y坐标+0.5
        this.x = c+0.5;
        this.y = r+0.5;
    }
}