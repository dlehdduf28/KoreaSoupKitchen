package net.dongyeol.koreasoupkitchen;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 클래스 : SoupDataManager
 * assets/data_기간.xml에 저장된 무료 급식소의 모든 데이터를 ArrayList에 담아서 반환 하는 매니저
 */

public class SoupDataManager {
    public static final String TAG = "SoupDataManager";
    final String xmlUrl;

    Context context;

    // 파싱된 데이터의 갯수 //
    int     countItem;

    // 데이터가 모두 파싱 되었으면 true로 처리 한다. //
    private boolean dataParsingComplete = false;

    // 파싱된 전체의 데이터들을 담을 리스트 //
    ArrayList<SoupKitItem> soupItemList;

    public SoupDataManager(Context context)
    {
        this.context = context;
        // XML 파일이 위치한 경로를 저장 한다. //
        xmlUrl = context.getString(R.string.mainData_File);
        //  SoupKitItem용 리스트 생성 //
        soupItemList = new ArrayList<SoupKitItem>();
    }

    public ArrayList<SoupKitItem> getSoupItemList() {
        try {
            // 데이터 파싱이 완료 되면 매니저의 notifyAll()가 호출 되어 데이터를 받을 수 있다. //
            if (dataParsingComplete == false) {
                synchronized ( this ) {
                    wait();
                }
            }
        } catch ( InterruptedException e) {
            Log.e(TAG, e.toString());
        }

        return soupItemList;
    }

    public void dataParsingStart()
    {
        ParsingTask task = new ParsingTask(this);
        task.execute();
    }

    // 데이터가 모두 파싱 되었으면 true를 반환 한다. //
    public boolean isDataParsingComplete() {
        return dataParsingComplete;
    }

    // AsyncTask를 이용하여 백그라운드 파싱 작업과 메인 스레드에서의 프로그래스바 대화 상자 출력을 진행 한다. //
    private class ParsingTask extends AsyncTask<Object, Object, Object> {
        // ProgressDialog asyncDialog = new ProgressDialog(context);

        // 매니저와의 소통을 위해서 참조를 한다. //
        SoupDataManager manager;

        private ParsingTask(SoupDataManager mag)
        {
            manager = mag;
        }
        // 백그라운드 작업을 실행하기 전에 프로그래스바 대화상자를 출력 시킨다. //
        @Override
        protected void onPreExecute() {
            /*
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setTitle(context.getString(R.string.proDialog_title_frg_soup));
            asyncDialog.setMessage(context.getString(R.string.proDialog_content_frg_soup));

            asyncDialog.show();*/
        }

        // 백그라운드 작업을 진행 한다. //
        @Override
        protected Object doInBackground(Object ... values) // 가변 인자
        {
            try {
                AssetManager assetManager = context.getAssets();
                InputStream in = assetManager.open(xmlUrl);

                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document document = builder.parse(in);
                countItem = processDocument(document);

                Log.d(TAG, countItem + " soup item processed.");
                dataParsingComplete = true;
            } catch (Exception e)
            {
                Log.d(TAG, e.toString());
            }

            return null;
        }

        // 백그라운드 작업이 끝나면 프로그래스바 대화상자를 소멸 시킨다. //
        @Override
        protected void onPostExecute(Object result) {
            // asyncDialog.dismiss();
            // 데이터를 받기 위해 매니저에서 대기 중인 스레드가 있다면 깨운다. //
            synchronized ( manager ) {
                manager.notifyAll();
            }
            super.onPostExecute(result);
        }
    }

    private int processDocument(Document doc) {
        Element docEle = doc.getDocumentElement(); // <data-set> 태그의 Element 객체 얻기

        NodeList nodeList = docEle.getElementsByTagName("record");

        int count = 0;

        if ((nodeList != null ) && (nodeList.getLength() > 0)) {
            for(int i = 0; i < nodeList.getLength(); i++) {
                SoupKitItem soupItem = dissectNode(nodeList, i);

                if(soupItem != null) {
                    soupItemList.add(soupItem);
                    count++;
                }
            }
        }

        return count;
    }

    private SoupKitItem dissectNode(NodeList nodelist, int index)
    {
        SoupKitItem soupItem = null;

        try {
            Element entry = (Element) nodelist.item(index);

            // record 태그 안에 들은 Element들을 꺼낸다. //
            Element title    = (Element) entry.getElementsByTagName("facility").item(0);
            Element address  = (Element) entry.getElementsByTagName("address").item(0);
            Element location = (Element) entry.getElementsByTagName("place").item(0);
            Element date     = (Element) entry.getElementsByTagName("date").item(0);
            Element time     = (Element) entry.getElementsByTagName("time").item(0);
            Element contact  = (Element) entry.getElementsByTagName("contact").item(0);
            Element longitude = (Element) entry.getElementsByTagName("longitude").item(0);
            Element latitude  = (Element) entry.getElementsByTagName("latitude").item(0);
            Element dataDate  = (Element) entry.getElementsByTagName("checkdate").item(0);

            // 각 Element의 노드에서 String 데이터를 꺼낸다. //
            String titleValue = null;
            if (title != null) {
                Node firstChild = title.getFirstChild();
                if (firstChild != null) {
                    titleValue = firstChild.getNodeValue();
                }
            }

            String addressValue = null;
            if (address != null) {
                Node firstChild = address.getFirstChild();
                if (firstChild != null) {
                    addressValue = firstChild.getNodeValue();
                }
            }

            String locationValue = null;
            if (location != null) {
                Node firstChild = location.getFirstChild();
                if (firstChild != null) {
                    locationValue = firstChild.getNodeValue();
                }
            }

            String dateValue = null;
            if (date != null) {
                Node firstChild = date.getFirstChild();
                if (firstChild != null) {
                    dateValue = firstChild.getNodeValue();
                }
            }

            String timeValue = null;
            if (time != null) {
                Node firstChild = time.getFirstChild();
                if (firstChild != null) {
                    timeValue = firstChild.getNodeValue();
                }
            }

            String contactValue = null;
            if (contact != null) {
                Node firstChild = contact.getFirstChild();
                if (firstChild != null) {
                    contactValue = firstChild.getNodeValue();
                }
            }

            String longitudeValue = null;
            if (longitude != null) {
                Node firstChild = longitude.getFirstChild();
                if (firstChild != null) {
                    longitudeValue = firstChild.getNodeValue();
                }
            }

            String latitudeValue = null;
            if (latitude != null) {
                Node firstChild = latitude.getFirstChild();
                if (firstChild != null) {
                    latitudeValue = firstChild.getNodeValue();
                }
            }

            String dataDateValue = null;
            if (dataDate != null) {
                Node firstChild = dataDate.getFirstChild();
                if (firstChild != null) {
                    dataDateValue = firstChild.getNodeValue();
                }
            }

            /* // debug //
            Log.d("Data", "Record node : " + titleValue + ", " + addressValue + ", " + locationValue + ", " +
                    dateValue + ", " + timeValue + ", " + contactValue + ", " + longitudeValue + ", " +
                    latitudeValue + ", " + dataDateValue);
            */
            soupItem = new SoupKitItem(titleValue,  addressValue, locationValue, dateValue, timeValue,
                    contactValue, longitudeValue, latitudeValue, dataDateValue);
        }catch(DOMException e)
        {
            Log.e(TAG, e.toString());
        }

        return soupItem;
    }
}
