<template>
    <Playground v-if="$store.state.pk.status === 'playing'"></Playground>
    <Matchground v-if="$store.state.pk.status === 'matching'"></Matchground>
    <ResultBoard v-if="$store.state.pk.loser !='none'"></ResultBoard>
</template>
<script>
import Playground from "@/components/PlayGround.vue";
import Matchground from "@/components/MatchingGround.vue";
import { onMounted,onUnmounted } from "vue";
import { useStore } from "vuex";
import ResultBoard from "@/components/ResultBoard.vue";
export default{
    components:{
        Playground,
        Matchground,
        ResultBoard
    },
    setup(){
        const store = useStore();
        const socketUrl = `ws://localhost:3000/websocket/${store.state.user.token}/`;
        let socket = null;
        onMounted(()=>{
            store.commit("updateOpponent",{
                username:"我的对手",
                photo:"https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            })
            socket = new WebSocket(socketUrl);
            socket.onopen=()=>{
                console.log("connected");
                store.commit("updatesocket",socket);
            }
            socket.onmessage = msg =>{
                const data = JSON.parse(msg.data);
                if(data.event === "match_found"){
                    store.commit("updateOpponent",{
                        username:data.opponent_username,
                        photo:data.opponent_photo
                    });
                    //alert("Match Found");
                    setTimeout(()=>{
                        store.commit("updateStatus","playing");
                    },3000);
                    store.commit("updateGame",data.game);
                    
                }else if(data.event === "move"){
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0,snake1] = game.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                }else if(data.event === "result"){
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    const [snake0,snake1] = game.snakes;
                    if(data.loser === "both" || data.loser === "A"){
                        snake0.status = "die";
                    }
                    if(data.loser === "both" || data.loser === "B"){
                        snake1.status = "die";
                    }
                    store.commit("updateLoser",data.loser);
                }
            }
            socket.onclose=()=>{
                console.log("disconnected!");
            }
        })


        onUnmounted(()=>{
            socket.close();
            store.commit("updateStatus","matching");
        })
    }
}
</script>
<style scoped>
</style>