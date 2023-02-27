<template>
    <div class="result-board">
        <div class="result-board-text" v-if="$store.state.pk.loser === 'both'">
            平局
        </div>
        <div class="result-board-text" v-else-if="$store.state.pk.loser === 'A' && $store.state.user.id == $store.state.pk.a_id ">
            失败
        </div>
        <div class="result-board-text" v-else-if="$store.state.pk.loser === 'B' && $store.state.user.id == $store.state.pk.b_id ">
            失败
        </div>
        <div class="result-board-text" v-else>
            胜利
        </div>
        <div class="result-board-btn">
            <button @click="restart" type="button" class="btn btn-warning btn-lg">再来一局</button>
        </div>
    </div>
</template>
<script>
import {useStore} from 'vuex';
export default{
    setup(){
        const store = useStore();
        const restart=()=>{
            store.commit("updateStatus","matching");
            store.commit("updateLoser","none");
            store.commit("updateOpponent",{
                username:"我的对手",
                photo:"https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            })
        }
        return{
            restart
        }
    }
}
</script>
<style scoped>
div.result-board{
    height:30vh;
    width:30vw;
    background-color: rgba(50,50,50,0.5);
    position: absolute;
    top:25vh;
    left: 35vw;
}
div.result-board-text{
    text-align: center;
    color:white;
    font-size: 50px;
    font-style: italic;
    padding-top: 5vh;
}
div.result-board-btn{
    padding-top: 10vh;
    text-align: center;
}
</style>