package net.dongyeol.koreasoupkitchen;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 클래스 : SouplistAdapter
 * 리스트뷰에서 급식소 뷰들을 관리하는 어댑터 클래스
 */
public class SoupListAdapter extends BaseAdapter {
    Context sContext;
    private List<SoupKitItem> list  = new ArrayList<SoupKitItem>();

    private final Object curFragment;

    public SoupListAdapter(Context context, Object curFragment)
    {
        super();
        sContext = context;
        this.curFragment = curFragment;
    }

    public void addItem(SoupKitItem item)
    {
        list.add(item);
    }

    public void setListItems(ArrayList<SoupKitItem> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        SoupKitItemView itemView;

        if(convertView == null)
            itemView = new SoupKitItemView(sContext, list.get(position));
        else {
            // 기존에 출력된 뷰를 재활용 하고 데이터를 수정 한다. //
            itemView = (SoupKitItemView) convertView;

            itemView.setSoupKitItem(list.get(position));
        }

        // 뷰의 대표 지역 설정 하기 //
        itemView.setRegionText();

        // 사용하는 프래그먼트가 FAVO 라면 저장 버튼의 기능을 삭제 기능으로 바꾼다.
        if(curFragment instanceof FavoFragment) {
            FavoFragment favoFragment = (FavoFragment) curFragment;
            itemView.favoButtonSet("삭제",
                    favoFragment.new FavoButtonListener(position));
        }


        return itemView;
    }

    public void listClear()
    {
        if(list != null)
            list.clear();
    }
}