<template>
  <NavBar />
  <router-view/>
</template>

<script>
import NavBar from "@/components/NavBar.vue";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap";
//import $ from 'jquery';

export default{
  components:{
    NavBar
  },
  setup(){
    // $.ajax({
    //   url:"http://localhost:3000/user/account/info/",
    //   type: "get",
    //   headers:{
    //     Authorization:"Bearer "+"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1ZWEyYTY0MDZkM2I0ZTZmOWQ2YzlhM2E4ODExZDkyOSIsInN1YiI6IjEiLCJpc3MiOiJzZyIsImlhdCI6MTY3Njc2ODU0MywiZXhwIjoxNjc3OTc4MTQzfQ.35AXG9M5RXOb_iiC-sXA07a58Bks2E66lgy2-PGMrEo",
    //   },
    //   success(resp){
    //     console.log(resp)
    //   }
    // });
    // $.ajax({
    //   url:"http://localhost:3000/user/account/register/",
    //   type:"post",
    //   data:{
    //     username:"cxy",
    //     password:"cxyxyc",
    //     confirmedPassword:"cxyxyc",
    //   },
    //   success(resp){
    //     console.log(resp);
    //   },
    //   error(resp){
    //     console.log(resp);
    //   }
    // })
  }
}
</script>
<style>
body{
  background-image: url("@/assets/images/background.png");
  background-size: cover;
}
</style>