const GAME_OBJECTS =[];

//对于每个对象每一帧都会执行一个操作
//如果这个对象没有进行过start，就执行start函数，否则执行update函数
export class GameObject{
    constructor(){
        GAME_OBJECTS.push(this);
        this.timedelta = 0;
        this.has_called_start = false;
    }
    start(){

    }
    update(){

    }
    on_destroy(){

    }
    destroy(){
        this.on_destroy();
        for(let i in GAME_OBJECTS){
            const obj = GAME_OBJECTS[i];
            if(obj == this){
                GAME_OBJECTS.splice(i);
                break;
            }
        }
    }
}
let last_timestamp;
const step = timestamp=>{
    for(let obj of GAME_OBJECTS){
        if(!obj.has_called_start){
            obj.start();
            obj.has_called_start = true;
        }else{
            obj.timedelta = timestamp - last_timestamp;
            obj.update();
        }
    }
    last_timestamp = timestamp;
    requestAnimationFrame(step)
}
requestAnimationFrame(step)