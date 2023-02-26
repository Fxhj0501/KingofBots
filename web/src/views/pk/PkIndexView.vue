<template>
    <Playground v-if="$store.state.pk.status === 'playing'"></Playground>
    <Matchground v-if="$store.state.pk.status === 'matching'"></Matchground>
</template>
<script>
import Playground from "@/components/PlayGround.vue";
import Matchground from "@/components/MatchingGround.vue";
import { onMounted,onUnmounted } from "vue";
import { useStore } from "vuex";
export default{
    components:{
        Playground,
        Matchground
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
                    store.commit("updateGamemap",data.gamemap);
                    
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