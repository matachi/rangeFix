package ca.uwaterloo.gsd.rangeFix
import java.io._
import java.io.FileReader
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.Map.Entry
import collection._
import ConditionalCompilation._
import scala.collection.immutable.TreeMap 
import scala.collection.mutable.LinkedHashMap 
class myNewCompareFile(loader:KconfigLoader,firFile:String,secFile:String) extends FunSuite{
	   def getResult():LinkedHashMap[String,Literal]={
            val (firVariable,firResultMap)=readFromFile(firFile)
            val (secVariable,secResultMap)=readFromFile(secFile)
            var firIterator=firResultMap.keySet.iterator
            var secIterator=secResultMap.keySet.iterator
            var firMissMap=TreeMap[String,Literal]()
            var firAddMap=TreeMap[String,Literal]()
            var firNotEqMap=TreeMap[String,Literal]()
            var secNotEqMap=TreeMap[String,Literal]()
            var firflag=false//标记第二个config文件是否结束
            var secflag=false//标记第二个config文件是否结束
            var firKey=firIterator.next
            var secKey=secIterator.next
            while(!firflag)
            {
               if(secflag==false)
               {
                    if(firKey==secKey)//如果变量名相同但值不相同
                    {
                        if((firResultMap(firKey)!=secResultMap(secKey)))
                        {
                            firNotEqMap+=(firKey->firResultMap(firKey))//保存第一个config文件里的变量名和值
                            secNotEqMap+=(secKey->secResultMap(secKey))//保存第二个config文件里的变量名和值
                        }
                        if(firIterator.hasNext)
                            firKey=firIterator.next
                        else firflag=true
                        if(secIterator.hasNext)
                            secKey=secIterator.next
                        else secflag=true
                    }
                    else if(firKey<secKey)//说明第一个config文件里多了个变量
                    {
                        firAddMap+=(firKey->firResultMap(firKey))//保存第一个config文件里的变量名和值
                        
                        if(firIterator.hasNext)
                            firKey=firIterator.next
                        else firflag=true
                    }
                    else if(firKey>secKey)//说明第二个config文件里多了个变量
                    {
                        firMissMap+=(secKey->secResultMap(secKey))//保存第二个config文件里的变量名和值
                        if(secIterator.hasNext)
                            secKey=secIterator.next
                        else secflag=true
                    }
                }
                else//第二个config比第一个先结束
                {
                    firAddMap+=(firKey->firResultMap(firKey))//保存第一个config文件里的变量名和值
                    if(firIterator.hasNext)
                        firKey=firIterator.next
                    else firflag=true
                }
            }
            while(!secflag)//第一个config比第二个先结束，说明第二个config文件里多了个变量
            {
                firMissMap+=(secKey->secResultMap(secKey))//保存第二个config文件里的变量名和值
                if(secIterator.hasNext)
                    secKey=secIterator.next
                else secflag=true
            } 
            
            var modifyMap=getModifyMap(loader,firMissMap,firAddMap,firNotEqMap,secNotEqMap,firVariable,secVariable)
            return modifyMap   
    }
    def getModifyMap(loader:KconfigLoader,firMissMap:TreeMap[String,Literal],firAddMap:TreeMap[String,Literal],firNotEqMap:TreeMap[String,Literal],secNotEqMap:TreeMap[String,Literal],
        firVariable:List[String],secVariable:List[String])
    :LinkedHashMap[String,Literal]={
         var fircount=0
         var seccount=0
         var modifyVariableMap=LinkedHashMap[String,Literal]()//新建Map存放config变量修改的变量，在第一个config的基础上进行改
         var firNum=firVariable.size
         var secNum=secVariable.size
         while(seccount<secNum)
         {
             var str=secVariable(seccount)
             if(firMissMap.contains(str) && (loader.getEffectiveIndex(str)!=None))
                    modifyVariableMap+=(str->firMissMap(str))
             else if(firNotEqMap.contains(str) && (loader.getEffectiveIndex(str)!=None))
                    modifyVariableMap+=(str->secNotEqMap(str))
             seccount=seccount+1
         }
         while(fircount<firNum)
         {
            var str=firVariable(fircount)
            if(firAddMap.contains(str) && (loader.getEffectiveIndex(str)!=None))
            {
                 var value=firAddMap(str)
                 var myvalue=
                 value match
                 {
                    case IntLiteral(temp)=>IntLiteral(0)
                    case StringLiteral(temp)=>StringLiteral("")
                    case _=>Kconfig.tristateNo
                 }
                 modifyVariableMap+=(str->myvalue)

            }
            fircount=fircount+1
         }
        //println(modifyVariableMap)
        return modifyVariableMap
    }
    def readFromFile(fileName:String):(List[String],TreeMap[String,Literal])={
            val tempMap=DotConfigParser.myParseFile(fileName).map { case (k, v) => Kconfig.encode(k) -> v.toExpression}
            var resultMap=new TreeMap[String,Literal]()
            var arrVariable=List[String]()//存放config变量
            var i = tempMap.keySet.iterator//迭代器
            while(i.hasNext){//遍历   
                var str = i.next
                resultMap+=(str->tempMap(str))
                arrVariable=arrVariable:::List(str)
            }
           return (arrVariable,resultMap)//返回config里的变量和重新排序后的Map 
    }
}


