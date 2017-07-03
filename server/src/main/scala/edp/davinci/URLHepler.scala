package edp.davinci


case class URLHelper(f:Option[String]=None,p:Option[Seq[KV]]=None){
  lazy val f_get: String = f.orNull
  lazy val p_get: Seq[KV] = p.orNull
}

case class KV(k:String,v:String)
