## DLTest
###百度dl框架研究学习demo
####关于dl框架
1. 插件加载框架
2. 与DLoad（动态加载，我的另一个工程）原理一样，但是封装处理的很好，Dload在此
3. 在插件（动态包中）支持R访问资源（在Dload中，我们是通过流来获取图片资源的）
4. 可维护多个插件（动态包），Dload中，我们只做了加载一个动态包的功能
5. 实际开发中，可直接在dl框架上修改使用即可，还是比较实用的
6. 虽然也可以加载还有.so文件的插件（动态包，下面都统一叫做“插件”），但是会有好多问题的

----
####先上demo，一睹为快
<img src="http://i.imgur.com/AYYo2Cz.png" width = "230" height = "350" alt="首页"/>
>#####demo首页，显示插件列表，有两个插件：main_plugin_a和main_plugin_b

---
#
<img src="http://i.imgur.com/x4U0f7F.png" width = "230" height = "350" alt="首页"/>
>#####进入main_plugin_a中


---
#
<img src="http://i.imgur.com/s2bPHZD.png" width = "230" height = "350" alt="首页"/>
>#####开启main_plugin_a中的service

---
#

<img src="http://i.imgur.com/yAjzNYO.png" width = "230" height = "350" alt="首页"/>
>#####关闭main_plugin_a中的service

---
#
<img src="http://i.imgur.com/Jh2S24g.png" width = "230" height = "350" alt="首页"/>
>#####打开main_plugin_a中另一个activity

---
#

<img src="http://i.imgur.com/31Tcm5i.png" width = "230" height = "350" alt="首页"/>

>进入插件main_plugin_b

---
#

<img src="http://i.imgur.com/iEqS3sv.png" width = "230" height = "350" alt="首页"/>
>在插件b中调用宿主方法

---
#
<img src="http://i.imgur.com/4gXfnoA.png" width = "230" height = "350" alt="首页"/>
>在宿主中调用插件main_plugin_b中的方法

---
#
####分析一下demo代码
#####看一下宿主工程
>MainActivity 的initData方法


		 private void initData() {
				//得到插件存放的位置，demo中用到的插件存放到了sdcard的dltest文件夹下
		        String pluginFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"dltest";
		        File file = new File(pluginFolder);
		
		        File[] plugins = file.listFiles();
		        if (plugins == null || plugins.length == 0) {
		            mNoPluginTextView.setVisibility(View.VISIBLE);
		            return;
		        }
				//遍历dltest文件夹下所有插件
		        for (File plugin : plugins) {
					//PluginItem是我自己定义的类
		            PluginItem item = new PluginItem();
		            item.pluginPath = plugin.getAbsolutePath();
					//使用DlUtils获取插件包信息
		            item.packageInfo = DLUtils.getPackageInfo(this, item.pluginPath);
		            if (item.packageInfo.activities != null && item.packageInfo.activities.length > 0) {
						//得到插件的主activity
		                item.launcherActivityName = item.packageInfo.activities[0].name;
		            }
		            if (item.packageInfo.services != null && item.packageInfo.services.length > 0) {
		                item.launcherServiceName = item.packageInfo.services[0].name;
		            }
		            mPluginItems.add(item);
					//加载插件，第二个参数为false，标识没有.so文件
		            DLPluginManager.getInstance(this).loadApk(item.pluginPath,false);
		        }
		
		        mListView.setAdapter(mPluginAdapter);
		        mListView.setOnItemClickListener(this);
		        mPluginAdapter.notifyDataSetChanged();
		    }

---
>点击ListView中对应条目（会调起插件）


	  @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        PluginItem item = mPluginItems.get(position);
	        DLPluginManager pluginManager = DLPluginManager.getInstance(this);
			//打开插件的中的Activity
	        pluginManager.startPluginActivity(this, new DLIntent(item.packageInfo.packageName, item.launcherActivityName));
	    }

---
#####看看main_plugin_a插件中
	//继承dl框架中的DLBasePluginActivity
	public class MainActivity extends DLBasePluginActivity implements View.OnClickListener {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //正常使用布局文件
	        setContentView(R.layout.activity_main);
	        findViewById(R.id.btn_openSecond).setOnClickListener(this);
	        findViewById(R.id.btn_openService).setOnClickListener(this);
	        findViewById(R.id.btn_stopService).setOnClickListener(this);
	    }
	    //开启插件中另一个Activity
	    public void openSecond(){
	        startPluginActivity(new DLIntent(getPackageName(),SecondTestActivity.class));
	    }
	    //开启服务
	    private void openService() {
	        startPluginService(new DLIntent(getPackageName(),TestService.class));
	    }
	    //停止服务
	    private void stopService() {
	        stopPluginService(new DLIntent(getPackageName(),TestService.class));
	    }
	    @Override
	    public void onClick(View v) {
	        switch (v.getId()){
	            case R.id.btn_openSecond:
	                openSecond();
	                break;
	            case R.id.btn_openService:
	                openService();
	                break;
	            case R.id.btn_stopService:
	                stopService();
	                break;
	        }
	    }
	}

>在插件中，所有的Activity都要继承dl中的DLBasePluginActivity，所有的FragmentActivity都要继承dl中的DLBasePluginFragmentActivity，所有的service都要继承dl中的DLBasePluginService，具体使用请看demo即可


>开启Activity使用startPluginActivity(DLIntent)；开启服务使用startPluginService(DLIntent)；停止服务使用stopPluginService(DLIntent），当然也可以绑定服务的，有相关方法

---
#####看看main_plugin_b,插件b实现了与宿主的互相调用

>插件b中

	public class MainActivity extends DLBasePluginFragmentActivity {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        findViewById(R.id.btn_plugin_b).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
					//得到宿主实现的接口对象
	                IHost host = InterfaceManager.getHost();
	                if(host==null){
	                    Toast.makeText(MainActivity.this.that,"hello", Toast.LENGTH_SHORT).show();
	                    return;
	                }
					//调用宿主方法
	                String hostMethod = host.hostMethod(that,"在插件main_plugin_b中调用>>");
	                Toast.makeText(MainActivity.this.that, hostMethod, Toast.LENGTH_SHORT).show();
	            }
	        });
			//传入插件b实现的接口对象，宿主得到后可以调用其方法
	        InterfaceManager.setPlugin(new IPlugin() {
	            @Override
	            public String pulginMethod(String s) {
	                return s+"我是插件b";
	            }
	        });
	    }
	}
	
>IHost、IPlugin和InterfaceManager没有在dl的library中，存在于我写的另一个library中，在这个类库中，主要定义插件和宿主之间调用的接口，demo中只是定义了一个简单的接口，详情请看demo中interactlib。

---
>在宿主中调用插件b实现IPlugin接口的对象的方法


	 public void doActionWithPlugin(View view){
	        String s = InterfaceManager.getPlugin().pulginMethod("在host中调用>>>");
	        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
	    }


	
---
####插件打包使用


