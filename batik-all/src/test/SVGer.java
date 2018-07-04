import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.VisitorSupport;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * 处理svg文件
 * Created by deanchen on 2017/1/17.
 */
public class SVGer {

  Document document;

  public void load(String content) {
    try {
      document = parseText(content);
    } catch (DocumentException e) {
      throw new RuntimeException( "SVG格式错误,不是合法的XML文件！");
    }
    Element root = document.getRootElement();
    if (!root.getName().equalsIgnoreCase("svg")) {
      throw new RuntimeException( "SVG格式错误,应该以svg作为跟元素");
    }

  }

  private static final class NameSpaceCleaner extends VisitorSupport {
    public void visit(Document document) {
      //            ((DefaultElement) document.getRootElement())
      //                    .setNamespace(Namespace.NO_NAMESPACE);
      //            document.getRootElement().additionalNamespaces().clear();
    }

    public void visit(Namespace namespace) {
      namespace.detach();
    }

    public void visit(Attribute node) {
      if (node.toString().contains("xmlns") || node.toString().contains("xsi:")) {
        node.detach();
      }
    }

    public void visit(Element node) {
      if (node instanceof DefaultElement) {
        ((DefaultElement) node).setNamespace(Namespace.NO_NAMESPACE);
      }
    }
  }

  private Element parseFragment(String fragment) {
    Document frag = null;
    try {

      frag = DocumentHelper.parseText(fragment);
      //            log.info(ff("set the fragment to namespace of document:%s",document.getRootElement().getNamespace()));
      //            ((DefaultElement)frag.getRootElement()).setNamespace(document.getRootElement().getNamespace());//Namespace.NO_NAMESPACE);
      //            log.info(ff("xml before remove namespace:%s",frag.asXML()));
      ////            //remove the namespace
      ////            ((DefaultElement) frag.getRootElement())
      ////                    .setNamespace(Namespace.NO_NAMESPACE);
      ////            frag.getRootElement().additionalNamespaces().clear();
      //
      //
      ////            ((DefaultElement)frag.getRootElement()).setNamespace(document.getRootElement().getNamespace());
      //
      //            frag.accept(new NameSpaceCleaner());
    } catch (DocumentException e) {
      throw new RuntimeException( "SVG格式错误，svg片段不是合法的XML格式！");
    }
    if (!frag.getRootElement().getName().equalsIgnoreCase("g")) {
      throw new RuntimeException( "SVG格式错误，svg片段必须以g元素作为跟节点！");
    }
    return frag.getRootElement();
  }

  public void addFragement(String objectid, String fragment) {
    Element frag = parseFragment(fragment);
    Element root = document.getRootElement();
    if (frag.getName() != null && frag.getName().equalsIgnoreCase("g")
        && frag.attribute("objectid") != null
        && frag.attribute("objectid").getValue().equals(objectid)) {
      List list = root.content();
      list.add(frag);
      //            Object addedNodeObj = list.get(list.size()-1);
      //            if(addedNodeObj instanceof DefaultElement){
      //                log.info(ff("to remove the namespace,before:%s",((DefaultElement) addedNodeObj).asXML()));
      //                ((DefaultElement) addedNodeObj).getParent().accept(new NameSpaceCleaner());
      //                log.info(ff("to remove the namespace,after:%s",((DefaultElement) addedNodeObj).asXML()));
      //            }
    } else {
      throw new RuntimeException( String.format(
          "svg片段格式错误，必须具有objectid属性，并且为指定的objectid值（%s）", objectid));
    }
  }

  public String output() {
    //        document.accept(new NameSpaceCleaner());
    return document.asXML().replace("xmlns=\"\"", "");
  }

  public String parse(String input) {
    Document temp;
    String out;
    try {
      temp = parseText(input);
      final Writer writer = new StringWriter();
      new XMLWriter(writer).write(temp);
      out = writer.toString();
    } catch (DocumentException e) {
      throw new RuntimeException( "SVG格式错误,不是合法的XML文件！");
    } catch (IOException e) {
      throw new RuntimeException( "SVG格式错误,不是合法的XML文件！");
    }
    Element root = temp.getRootElement();
    if (!root.getName().equalsIgnoreCase("svg")) {
      throw new RuntimeException( "SVG格式错误,应该以svg作为跟元素");
    }
    //        temp.setEntityResolver(new NoOpEntityResolver());
    //        return temp.asXML().replace("xmlns=\"\"","");

    return out.replace("xmlns=\"\"", "");
    //        return out.replace("xmlns=\"\"","");

    //        return input.replace("xmlns=\"\"","");
  }

  public static Document parseText(String text) throws DocumentException {
    SAXReader reader = new SAXReader();
    //        reader.setValidation(true);
    reader.setIncludeInternalDTDDeclarations(true);
    //        reader.
    reader.setIncludeExternalDTDDeclarations(true);
    String encoding = getEncoding(text);
    InputSource source = new InputSource(new StringReader(text));
    source.setEncoding(encoding);
    Document result = reader.read(source);
    if (result.getXMLEncoding() == null) {
      result.setXMLEncoding(encoding);
    }

    return result;
  }

  private static String getEncoding(String text) {
    String result = null;
    String xml = text.trim();
    if (xml.startsWith("<?xml")) {
      int end = xml.indexOf("?>");
      String sub = xml.substring(0, end);
      StringTokenizer tokens = new StringTokenizer(sub, " =\"'");

      while (tokens.hasMoreTokens()) {
        String token = tokens.nextToken();
        if ("encoding".equals(token)) {
          if (tokens.hasMoreTokens()) {
            result = tokens.nextToken();
          }
          break;
        }
      }
    }

    return result;
  }

  public String getFragment(String objectid) {
    Element root = document.getRootElement();
    for (Iterator i = root.elementIterator(); i.hasNext();) {
      Element element = (Element) i.next();
      if (element.getName() != null && element.getName().equalsIgnoreCase("g")
          && element.attribute("objectid") != null
          && element.attribute("objectid").getValue().equals(objectid)) {
        return element.asXML();
      }
    }
    throw new RuntimeException( String.format("找不到id 为： %s 的svg片段!", objectid));
  }

  public void modifyFragment(String objectid, String fragment) {
    Element frag = parseFragment(fragment);
    Element root = document.getRootElement();
    int index = 0;
    //        for(Iterator i = root.elementIterator(); i.hasNext();){
    //            Element element = (Element)i.next();
    //            if(element.getName() != null && element.getName().equalsIgnoreCase("g") && element.attribute("objectid") != null && element.attribute("objectid").getValue().equals(objectid)){
    //                log.trace(ff("the index of bingo element:%s, the list size:%s",index,root.content().size()));
    //                root.remove(element);
    //                root.content().add(index + 2,frag);
    //                //root.add(frag);
    //                return;
    //            }
    //            index++;
    //        }
    for (Iterator i = root.content().listIterator(); i.hasNext();) {
      Object next = i.next();
      if (!(next instanceof Element)) {
        index++;
        continue;
      }
      Element element = (Element) next;
      if (element.getName() != null && element.getName().equalsIgnoreCase("g")
          && element.attribute("objectid") != null
          && element.attribute("objectid").getValue().equals(objectid)) {
        root.content().remove(index);
        root.content().add(index, frag);
        //root.add(frag);
        return;
      }
      index++;
    }
    throw new RuntimeException( String.format("找不到id 为： %s 的svg片段!", objectid));
  }

  public void delFragment(String objectid) {
    Element root = document.getRootElement();
    for (Iterator i = root.elementIterator(); i.hasNext();) {
      Element element = (Element) i.next();
      if (element.getName() != null && element.getName().equalsIgnoreCase("g")
          && element.attribute("objectid") != null
          && element.attribute("objectid").getValue().equals(objectid)) {
        root.remove(element);
        return;
      }
    }
    throw new RuntimeException( String.format("找不到id 为： %s 的svg片段!", objectid));
  }

  public class NoOpEntityResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) {
      return new InputSource(new StringBufferInputStream(publicId + " " + systemId));
    }
  }

}
