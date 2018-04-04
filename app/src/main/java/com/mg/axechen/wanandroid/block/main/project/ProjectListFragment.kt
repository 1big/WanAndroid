package com.mg.axechen.wanandroid.block.main.project

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.mg.axechen.wanandroid.R
import com.mg.axechen.wanandroid.block.details.WebViewActivity
import com.mg.axechen.wanandroid.block.main.home.CustomLoadMoreView
import com.mg.axechen.wanandroid.javabean.HomeData
import com.mg.axechen.wanandroid.javabean.ProjectListBean
import com.mg.axechen.wanandroid.javabean.TreeBean
import kotlinx.android.synthetic.main.fragment_project_list.*
import kotlinx.android.synthetic.main.item_knowledge_tree_list.*
import network.schedules.SchedulerProvider

/**
 * Created by AxeChen on 2018/4/2.
 *
 * 项目列表
 */
class ProjectListFragment : Fragment(), ProjectListContract.View {

    private var kinds: List<TreeBean> = mutableListOf<TreeBean>()

    private var projects = mutableListOf<HomeData>()

    private var selectProject: TreeBean? = null

    private var listAdapter: ProjectListAdapter? = null

    private val kindsAdapter: KindsAdapters by lazy {
        KindsAdapters(R.layout.item_spinner_kinds, kinds)
    }

    private val presenter: ProjectListContract.Presenter by lazy {
        ProjectListPresenter(SchedulerProvider.getInstatnce()!!, this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_project_list, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        drawerLayout.setScrimColor(Color.TRANSPARENT)
        initKindsClick()
        initRefresh()
        presenter.getProjectTree()
    }

    override fun getProjectTreeSuccess(bean: List<TreeBean>) {
        kinds = bean
        selectProject = kinds[0]
        initKindsAdapter()
        kindsAdapter.setSelect(selectProject!!)
        tvKind.text = selectProject!!.name
        presenter.getProjectTreeList(selectProject!!.id, true)
        kindsAdapter.setOnItemClickListener { adapter, view, position ->
            // 关闭侧滑。请求数据
            drawerLayout.closeDrawer(flRight)
            selectProject = kinds[position]
            listAdapter?.loadMoreEnd(true)
            kindsAdapter.setSelect(selectProject!!)
            tvKind.text = selectProject!!.name
            presenter.getProjectTreeList(selectProject!!.id, true)
            kindsAdapter.notifyDataSetChanged()
        }
    }

    private fun initKindsClick() {
        tvKind.setOnClickListener { view ->
            changeRightPage()
        }
    }

    private fun changeRightPage() {
        if (drawerLayout.isDrawerOpen(flRight)) {
            drawerLayout.closeDrawer(flRight)
        } else {
            drawerLayout.openDrawer(flRight)
        }
    }

    private fun initKindsAdapter() {
        rvKinds.layoutManager = LinearLayoutManager(activity)
        rvKinds.adapter = kindsAdapter
    }

    private fun initProjectsAdapter() {
        rvList.layoutManager = LinearLayoutManager(activity)
        listAdapter = ProjectListAdapter(R.layout.item_project_list, projects)

        rvList.adapter = listAdapter
        listAdapter?.setPreLoadNumber(0)
        listAdapter?.setEnableLoadMore(true)
        listAdapter?.setLoadMoreView(CustomLoadMoreView())
        listAdapter?.setOnLoadMoreListener(BaseQuickAdapter.RequestLoadMoreListener {
            presenter.getProjectTreeList(selectProject!!.id, false)
        }, rvList)
        listAdapter?.setOnItemClickListener { adapter, view, position ->
            var homeData: HomeData = adapter.data.get(position) as HomeData
            WebViewActivity.lunch(activity, homeData.link, homeData.title)
        }
    }

    override fun getProjectTreeFail(msg: String) {

    }

    private fun initRefresh() {
        sRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            presenter.getProjectTreeList(selectProject!!.id, true)
        })
    }

    override fun getProjectListByCidSuccess(bean: ProjectListBean, isRefresh: Boolean) {
        sRefresh.isRefreshing = false
        listAdapter?.loadMoreComplete()
        if (isRefresh) {
            projects.clear()
            projects = bean.datas
            listAdapter?.setNewData(projects)

            if (listAdapter == null) {
                initProjectsAdapter()
            } else {
                listAdapter?.notifyDataSetChanged()
            }

            // 计算页数，是否开启加载下一页
            if (bean.size >= bean.total) {
                listAdapter?.setEnableLoadMore(false)
            }
        } else {
            if (bean.datas.size != 0) {
                listAdapter?.loadMoreEnd(false)
                projects.addAll(bean.datas)
                listAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun getProjectListByCidFail(msg: String) {
        listAdapter?.loadMoreComplete()
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }


}