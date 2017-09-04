/*-
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package edp.davinci.csv

import java.io.ByteArrayOutputStream


object CSVTest extends App {
  val baos = new ByteArrayOutputStream()
  val writer = CSVWriter.open(baos)
  val a = List(Seq("a", "b b  ", "c/ \"\\cc"), Seq("a", "b b  ", "casfa,", ""))
  val CSVArr = a.map(s => {
    writer.writeRow(s)
    val csv=baos.toString("UTF-8")
    baos.reset()
    csv
  })
  CSVArr.foreach(println)
  writer.close()
  baos.close()

  //  val w = writer.writeRow(a)
  //  println(baos.toString("UTF-8"))
}
