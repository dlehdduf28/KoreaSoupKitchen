package net.dongyeol.koreasoupkitchen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 프래그먼트 클래스 : FavoFragment
 * 저장된 급식소를 출력한다.
 */

public class FavoFragment extends Fragment {
    FavoDataManager dataManager;

    private TextView resultText;
    private ListView listView;
    private SoupListAdapter listAdapter;

    private ArrayList<SoupKitItem> soupItemList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_favo, container, false);

        resultText = (TextView) rootView.findViewById(R.id.resultText_frg_Favo);
        listView   = (ListView) rootView.findViewById(R.id.listView_frg_Favo);

        dataManager = FavoDataManager.getInstance(getActivity());

        listAdapter = new SoupListAdapter(getActivity(), this);
        listView.setAdapter(listAdapter);

        dataManager.prepareManager();

        return rootView;
    }

    // 리스트뷰에 저장소에 저장된 객체들을 출력 시킨다.
    public void setListView()
    {
        soupItemList = dataManager.readSoupItemList();

        if(soupItemList == null)
            return;

        // 리스트뷰의 어댑터에 데이터를 반영 하고 리스트뷰에 대입 한다. //
        listAdapter.setListItems(soupItemList);
        listAdapter.notifyDataSetChanged();
        resultText.setText(soupItemList.size() + getString(R.string.resultText_frg_soup));
    }

    // Inner class
    public class FavoButtonListener implements View.OnClickListener {
        final int idx;
        public FavoButtonListener(int idx)
        {
            this.idx  =  idx;
        }

        @Override
        public void onClick(View v)
        {
            FavoDataManager dataManager = FavoDataManager.getInstance(getActivity());
            dataManager.deleteSoupKitItem(idx);
            // 삭제된 객체가 있으므로 리스트뷰를 다시 출력 한다. //
            setListView();

            Snackbar.make(getView(), getString(R.string.delete_frg_favo),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
