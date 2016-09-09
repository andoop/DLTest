## DLTest
###百度dl框架研究学习demo
###github:https://github.com/andoop/DLTest
####关于dl框架
1. 插件加载框架
2. 与DLoad（动态加载，我的另一个工程）原理一样，但是封装处理的很好，[Dload在此](https://github.com/andoop/Dload)
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

插件应该从服务端下载，demo中为了演示方便，将插件APk通过adb命令push到了sdcard的dltest文件夹下

task命令如下

	def dlPath = '/sdcard/dltest'
	def upload = { ->
	        try {
				//创建dltest文件夹
	            exec {
	                commandLine 'E:/android_dev/sdk/sdk/platform-tools/adb.exe', 'shell', 'mkdir', dlPath
	            }
	        }
	        catch (ignored) {
	        }
	
	        try{
				//将所有工程中生成的apk上传到dltest文件夹中
	            exec {
	                commandLine 'E:/android_dev/sdk/sdk/platform-tools/adb.exe', 'push', project.name + '-debug.apk', dlPath
	                workingDir project.projectDir.toString() + '/build/outputs/apk/'
	            }
	        }
	        catch (ignored){}
	    }
	
	    task uploadDebug << {
	        upload()
	    }

最后执行uploadDebug任务即可

在这里不用通过dx命令再次处理插件包了，是因为这里的插件包是apk文件，apk文件中的.class文件已经被处理过了

---
####dl原理分析

#####占坑

     <activity android:name="com.ryg.dynamicload.DLProxyActivity"/>
        <activity android:name="com.ryg.dynamicload.DLProxyFragmentActivity"/>
        <service android:name="com.ryg.dynamicload.DLProxyService"
            android:enabled="true"
            android:exported="true"/>
>插件中的Activity，FragmentActivity，service要分别继承dl的DLBasePluginActivity、DLBasePluginFragmentActivity和DLBasePluginService

>dl中的DLProxyActivity，DLProxyFragmentActivity，DLProxyService会代理插件中Activity，FragmentActivity，service的生命周期

---
#####插件中组件关联代理组件
以Activity为例：
DLProxyActivity实现了DLAttachable接口

	public interface DLAttachable {
	    public void attach(DLPlugin proxyActivity, DLPluginManager pluginManager);
	}

>他调用attach方法将被代理的Activity传过来（插件中继承了DLBasePluginActivity的Activity）


DLBasePluginActivity实现了DLPlugin,DLPlugin如下

	public interface DLPlugin {
	
	    public void onCreate(Bundle savedInstanceState);
	    public void onStart();
	    public void onRestart();
	    public void onActivityResult(int requestCode, int resultCode, Intent data);
	    public void onResume();
	    public void onPause();
	    public void onStop();
	    public void onDestroy();
		//调用这里的attach，将代理Activity传入到了DLBasePluginActivity中（插件中Activity）
	    public void attach(Activity proxyActivity, DLPluginPackage pluginPackage);
	    public void onSaveInstanceState(Bundle outState);
	    public void onNewIntent(Intent intent);
	    public void onRestoreInstanceState(Bundle savedInstanceState);
	    public boolean onTouchEvent(MotionEvent event);
	    public boolean onKeyUp(int keyCode, KeyEvent event);
	    public void onWindowAttributesChanged(LayoutParams params);
	    public void onWindowFocusChanged(boolean hasFocus);
	    public void onBackPressed();
	    public boolean onCreateOptionsMenu(Menu menu);
	    public boolean onOptionsItemSelected(MenuItem item);
	}

>都是Activity的生命周期方法

在DLProxyActivity中

	@Override
	    protected void onStart() {
	        mRemoteActivity.onStart();
	        super.onStart();
	    }
	
	    @Override
	    protected void onRestart() {
	        mRemoteActivity.onRestart();
	        super.onRestart();
	    }
	
	    @Override
	    protected void onResume() {
	        mRemoteActivity.onResume();
	        super.onResume();
	    }
	
	    @Override
	    protected void onPause() {
	        mRemoteActivity.onPause();
	        super.onPause();
	    }

>摘出了部分代码，mRemoteActivity就是被代理的DLBasePluginActivity（插件中的Activity）

---
#####插件中为什么可以通过R来访问图片呢？

重写Activity中的 getAssets()和getResources()并返回自己的重写构建AssetManager，（AssetManager中对应的资源路径，改为插件路径即可）和由此生成的Resources即可，

dl中体现如下：（DLPluginManager中）

	  AssetManager assetManager = createAssetManager(dexPath);
	  Resources resources = createResources(assetManager);

---
	private AssetManager createAssetManager(String dexPath) {
	        try {
	            AssetManager assetManager = AssetManager.class.newInstance();
	            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
	            addAssetPath.invoke(assetManager, dexPath);
	            return assetManager;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	
	    }
	
	    private Resources createResources(AssetManager assetManager) {
	        Resources superRes = mContext.getResources();
	        Resources resources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
	        return resources;
	    }
>dexPath即为插件路径，例如：/sdcard/dltest/xxx.apk

---
#####怎样实例化插件中类的呢（DLPluginManager中）

	 private DexClassLoader createDexClassLoader(String dexPath) {
	        File dexOutputDir = mContext.getDir("dex", Context.MODE_PRIVATE);
	        dexOutputPath = dexOutputDir.getAbsolutePath();
			//初始化一个dexclassloader
	        DexClassLoader loader = new DexClassLoader(dexPath, dexOutputPath, mNativeLibDir, mContext.getClassLoader());
	        return loader;
	    }

---
	       DexClassLoader dexClassLoader = createDexClassLoader(dexPath);
	        AssetManager assetManager = createAssetManager(dexPath);
	        Resources resources = createResources(assetManager);
	        // create pluginPackage
	        pluginPackage = new DLPluginPackage(dexClassLoader, resources, packageInfo);
	        mPackagesHolder.put(packageInfo.packageName, pluginPackage);

>每一个插件都会对应一个dexclassloder，生成的dexclassload会被封装到DLPluginPackage对象中，而这个对象会被放到以插件包名为键的map中，这样插件和dexclassloader就一一对应了

---

	  protected void launchTargetActivity() {
	        try {
				//根据类名生成一个插件中Activity的对象
	            Class<?> localClass = getClassLoader().loadClass(mClass);
	            Constructor<?> localConstructor = localClass.getConstructor(new Class[] {});
	            Object instance = localConstructor.newInstance(new Object[] {});
				//生成的插件中Activity的对象
	            mPluginActivity = (DLPlugin) instance;
				//代理Activity关联插件中的Activity
	            ((DLAttachable) mProxyActivity).attach(mPluginActivity, mPluginManager);
	            Log.d(TAG, "instance = " + instance);
	            // attach the proxy activity and plugin package to the mPluginActivity
				//插件中Activity关联代理Activity
	            mPluginActivity.attach(mProxyActivity, mPluginPackage);
	
	            Bundle bundle = new Bundle();
	            bundle.putInt(DLConstants.FROM, DLConstants.FROM_EXTERNAL);
	            mPluginActivity.onCreate(bundle);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
>上面是生成插件中Activity对象的过程，插件中service对象生成与关联代理的流程，是类似的，这个不过多讨论了

---
#####在插件中使用上下文时，慎用this
插件中的Activity最后是被当做普通的类，通过反射得到其实例的，系统不会管理器生命周期，通过this获取上下文已经不行了，但是dl为我们提供了关键字that，that会在不同的情况下，指向本Activity或者代理Activity，所以，在使用this行不通的情况下，可以使用that。

---
####注意点

插件和宿主之间的桥梁其实是接口，如demo中宿主和插件b之间相互调用的接口，宿主和插件b都依赖了interactlib工程，但是插件b打包的时候，却不能包含任何接口文件，否者会报异常，demo中处理如下：

	dependencies {
	    compile fileTree(include: ['*.jar'], dir: 'libs')
	    testCompile 'junit:junit:4.12'
		//去除v7中的v4包，防止跟宿主v4冲突
	    compile('com.android.support:appcompat-v7:23.4.0') {
	        exclude module: 'support-v4'
	    }
	    compile project(':dllib')
		//私有引用，打包时不会包含
	    provided files('../interactlib/build/libs/interactlib.jar')
	}
>上面是插件b的dependencies

---
以上就是dl的使用方法，还有它的原理解析，具体使用还是请参考demo，
#####更多学习>>dl 源码地址：
#####https://github.com/singwhatiwanna/dynamic-load-apk

#####欢迎关注andoop,周一、二内容更新，干货永不断！