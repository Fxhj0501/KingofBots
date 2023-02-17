import { GameObject } from "./GameObject";
import { Snake } from "./Snake";
import { Wall } from "./Wall";
export class GameMap extends GameObject{
    constructor(ctx,parent){
        super();
        this.ctx = ctx;
        this.parent = parent;
        this.L = 0;
        this.rows = 13;
        this.cols = 14;
        this.walls = [];
        this.inner_walls_count = 30;
        this.snakes = [
            new Snake({id:0,color:"#4876EC",r:this.rows-2,c:1},this),
            new Snake({id:1,color:"#F94848",r:1,c:this.cols-2},this),
        ];
        

    }
    //确保起点和重点在图里是联通的
    isConnected(g,startx,starty,endx,endy){
        if(startx==endx && starty==endy)
            return true;
        g[startx][starty] = true;
        let dx = [-1,0,1,0];
        let dy = [0,-1,0,1];
        for(let i=0;i<4;i++){
            let newx = startx+dx[i];
            let newy = starty+dy[i];
            if(newx>=0&&newx<this.rows-1&&newy>=0&&newy<this.cols-1&&!g[newx][newy] && this.isConnected(g,newx,newy,endx,endy))
                return true;
        }
        return false;
    }
    create_walls(){
        //记录当前单元格是否已经有墙了
        const positionW  = [];
        for(let r = 0;r<this.rows;r++){
            positionW[r] = [];
            for(let c=0;c<this.cols;c++){
                positionW[r][c] - false;
            }
        }
        //先给Gamemap的四周设置为墙
        for(let r= 0;r<this.rows;r++){
            positionW[r][0] = positionW[r][this.cols-1]=true;
        }
        for(let c= 0;c<this.cols;c++){
            positionW[0][c] = positionW[this.rows-1][c]=true;
        }
        //为了游戏的公平，障碍物是中心对称的
        for(let i=0;i<this.inner_walls_count/2;i++){
            for(let j=0;j<1000;j++){
                let r = parseInt(Math.random()*this.rows);
                let c = parseInt(Math.random()*this.cols);
                //保证中心对称
                if(positionW[this.rows-1-r][this.cols-1-c]||positionW[r][c])
                    continue;
                //左下角和右上角是初始位置，不能有障碍物
                if(r==this.rows-2 && c==1 || r==1 && c==this.cols-2)
                    continue;
                //中心对称
                positionW[r][c] = positionW[this.rows-1-r][this.cols-1-c] = true;
                break;
            }
        }
        const copypostionW = JSON.parse(JSON.stringify(positionW));
        if(!this.isConnected(copypostionW,this.rows-2,1,1,this.cols-2))
            return false;
        for(let r=0;r<this.rows;r++){
            for(let c=0;c<this.cols;c++){
                if(positionW[r][c]){
                    this.walls.push(new Wall(r,c,this));
                }
            }
        }
        return true;
    }
    add_listening_events(){
        this.ctx.canvas.focus();
        const [snake0,snake1] = this.snakes;
        this.ctx.canvas.addEventListener("keydown",e=>{
            if(e.key === 'w')
                snake0.set_direction(0);
            else if(e.key === 'd')
                snake0.set_direction(1);
            else if(e.key === 's')
                snake0.set_direction(2);
            else if(e.key === 'a')
                snake0.set_direction(3);
            else if(e.key === 'ArrowUp')
                snake1.set_direction(0);
            else if(e.key === 'ArrowRight')
                snake1.set_direction(1);
            else if(e.key === 'ArrowDown')
                snake1.set_direction(2);
            else if(e.key === 'ArrowLeft')
                snake1.set_direction(3);
        })
    }
    start(){
        for(let i=0;i<1000;i++){
            if(this.create_walls())
                break;
        }
        this.add_listening_events();
    }
    update_size(){
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols,this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L*this.cols;
        this.ctx.canvas.height = this.L*this.rows;
    }
    //判断两条蛇是否准备好了进入下一状态
    check_ready(){
        for(const snake of this.snakes){
            if(snake.status !== "idle")
                return false;
            if(snake.direction === -1)
                return false;
        }
        return true;
    }
    //让两条蛇进入下一回合
    next_step(){
        for(const snake of this.snakes){
            snake.next_step();
        }
    }
    //检测目标位置是否合法：没有撞到蛇的身体和墙壁
    check_valid(cell){
        for(const wall of this.walls){
            if(wall.r === cell.r && wall.c === cell.c){
                return false;
            }
        }
        for(const snake of this.snakes){
            let k = snake.cells.length;
            //当蛇尾会前进的时候，没有影响
            if(!snake.check_tail_increasing()){
                k--;
            }
            for(let i=0;i<k;i++){
                if(snake.cells[i].r === cell.r && snake.cells[i].c === cell.c){
                    return false;
                }
            }
        }
        return true;
    }
    update(){
        this.update_size();
        if(this.check_ready()){
            this.next_step();
        }
        this.render();
    }
    render(){
        const color_even = "#AAD751";
        const color_odd = "#A2D149";
        for(let r = 0;r<this.rows;r++){
            for(let c=0;c<this.cols;c++){
                if((r+c)%2==0){
                    this.ctx.fillStyle = color_even;
                }else{
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(c*this.L,r*this.L,this.L,this.L);
            }
        }
    }
}