import { createRouter, createWebHistory } from 'vue-router'
import PkIndexView from "../views/pk/PkIndexView";
import RecordIndexView from "../views/record/RecordIndexView";
import RankListIndexView from "../views/ranklist/RankListIndexView";
import UserBotIndexView from "../views/user/bots/UserBotIndexView";
import NotFound from "../views/error/NotFound";

const routes = [
  {
    path:"/",
    name:"home",
    redirect:"/pk/"
  },
  {
    path:"/pk/",
    name:"pk_index",
    component:PkIndexView,
  },
  {
    path:"/record/",
    name:"record_index",
    component:RecordIndexView,
  },
  {
    path:"/ranklist/",
    name:"ranklist_index",
    component:RankListIndexView,
  },
  {
    path:"/user/bots/",
    name:"user_bots_index",
    component:UserBotIndexView,
  },
  {
    path:"/404/",
    name:"404",
    component:NotFound,
  },
  {
    path:"/:catchAll(.*)",
    redirect:"/404/"
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
