package edp.davinci.util

import java.io.{BufferedWriter, File, FileWriter}

import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, MediaTypes}

object FileUtils extends FileUtils

trait FileUtils {
  def write2File(data: String, path: String) {
    try {
      val file = new File(path)
      //if file doesnt exists, then create it
      if (!file.exists()) {
        file.createNewFile()
      }
      //true = append file
      val fileWritter = new FileWriter(file.getName, true)
      val bufferWritter = new BufferedWriter(fileWritter)
      bufferWritter.write(data)
      bufferWritter.close()
      System.out.println("Done")
    } catch {
      case e: Throwable => e.printStackTrace();
    }
  }

  def downloadFile(path: String,data:String)= {

    val responseEntity = HttpEntity(MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`, data.getBytes("UTF-8"))

  }
}

