package ca.uwaterloo.gsd.rangeFix
import java.util.Date
object DiagnoseGenerator{
  def getDiagnoses[T](getCore : Traversable[T] => Option[List[T]]):List[List[T]]=
  {
    //var fr = new java.io.FileWriter("newAl.txt",true)
    //fr.write("----------------\n")
    var uncomplete:IndexedSeq[IndexedSeq[T]]=IndexedSeq[IndexedSeq[T]](IndexedSeq[T]()) // uncomplete set
    var complete:IndexedSeq[IndexedSeq[T]]=IndexedSeq[IndexedSeq[T]]() //complete set :diagnoses
    while ( uncomplete.size>0){
      val head = uncomplete.head //take an set from the uncomplete
      uncomplete= uncomplete.tail
      //fr.write("head:%s\n" format head)
      if ((complete++uncomplete).exists(cs=>cs.intersect(head).size>=cs.size)){
        //fr.write("\t is a super set\n")
      }
      else {
        val ops = getCore(head.toList)
        if (!ops.isDefined){
          //fr.write("no core\n")
          complete = complete.filter(_.intersect(head).size< head.size)
          complete = complete :+ head
        }
        else{
	  val core = ops.get.toIndexedSeq
          //fr.write("core:%s\n" format core)
          var app:IndexedSeq[IndexedSeq[T]]=IndexedSeq[IndexedSeq[T]]()
	  var oldIndexes:IndexedSeq[Int]=IndexedSeq[Int]()
          //get those not intersect
          for (i<- 0 until uncomplete.size){
            if (uncomplete(i).intersect(core).size<=0)
              oldIndexes = oldIndexes :+ i
          }
          // append
          core.foreach(c=>app = app:+ (head:+c))
          oldIndexes.foreach(i=>{
            core.foreach(c=>app=app:+(uncomplete(i):+c))
          })
          //remove orignal
          uncomplete = for (i<- (0 until uncomplete.size).filterNot(od=>oldIndexes.contains(od))) yield{
            uncomplete(i)
          }
          app = app.filterNot(a=>(complete++uncomplete).exists(us=>us.intersect(a).size>=us.size))
          //fr.write("app:%s\n" format app)
          uncomplete=uncomplete++app
        }
      }
      //fr.write("uncomplete:%s\n" format uncomplete)
      //fr.write("complete:%s\n" format complete)
    }
    //fr.close() 
    complete.map(_.toList).toList
  }
}
