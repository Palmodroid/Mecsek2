package digitalgarden.mecsek.diary;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import digitalgarden.mecsek.R;

public class MonthlyFragment extends Fragment
        implements View.OnClickListener, View.OnLongClickListener
    {
    // Store instance variables
    private int monthIndex;

    /**
     * newInstance constructor for creating fragment with arguments
     * @param monthIndex
     * 
     * sets actual month as getMonthIndex, which is equal the position in the MSEViewer
     *
     * @return
     */
    public static MonthlyFragment newInstance(int monthIndex )
        {
        MonthlyFragment fragmentFirst = new MonthlyFragment();
        Bundle args = new Bundle();
        args.putInt("MSE", monthIndex);

        // Longtime lt = new Longtime();
        // lt.setMonthIndex( getMonthIndex );
        // Log.d("TODAY", "Fragments month: " + lt.toString());

        // args.putLong("TODAY", today);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
        }


    ConnectionToActivity connectionToActivity;

    // onAttach is called first, before onCreate!!!
    @Override
    public void onAttach(Context context)
        {
        super.onAttach(context);

        try
            {
            connectionToActivity = (ConnectionToActivity) context;
            }
        catch (ClassCastException e)
            {
            throw new ClassCastException(context.toString() + " must implement " +
                    "ConnectionToActivity");
            }
        }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        monthIndex = getArguments().getInt("MSE");
        // today = getArguments().getLong("TODAY");
        }


    MonthlyData monthlyData;
    MonthlyLayout monthlyLayout;

    // Inflate the view for the fragment based on layout XML
    // onCreateView is called AFTER onAttach and onCreate, Activity's onCreate is ready
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
        {
        monthlyData =
                connectionToActivity.getDataStore().getMonthlyData(monthIndex);

        monthlyData.setMonthlyFragment(this);

        View view = inflater.
                inflate(R.layout.fragment_monthly_viewer, container, false);

        // secondary parameters, which cannot be passed bay constructor (View constructor cannot
        // be changed)
        monthlyLayout =
                ((MonthlyLayout)view.findViewById(R.id.diary_layout));

        monthlyLayout.setMonthlyData( monthlyData );
        monthlyLayout.setOnClickListener( this );
        monthlyLayout.setOnLongClickListener( this );

        TextView yearMonthTextView =
                (TextView) view.findViewById(R.id.year_month_text_view);
        yearMonthTextView.setText( monthlyData.getYearMonthString() );

        monthlyData.createLoader();

        return view;
        }

    public void onLoadFinished()
        {
        monthlyLayout.onLoadFinished();
        }

    @Override
    public void onClick(View v)
        {
        connectionToActivity.onReady( ((ComplexDailyView)v).getDailyData() );
        }

    @Override
    public boolean onLongClick(View v)
        {
        connectionToActivity.onLongClickDetected( ((ComplexDailyView)v).getDailyData() );
        return true;
        }

    @Override
    public void onDestroyView()
        {
        super.onDestroyView();

        // elfordításnál itt probléma lesz!! Vagyis kilövi, holott nem kellene.
        monthlyData.destroyLoader();
        }


    @Override
    public void onDetach()
        {
        super.onDetach();


        }

    }
