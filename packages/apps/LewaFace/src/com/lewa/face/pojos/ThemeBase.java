package com.lewa.face.pojos;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

public class ThemeBase implements Serializable{

	private static final long serialVersionUID = 2866915953243943771L;
	private String cnName;
	private String enName;
	private String cnAuthor;
	private String enAuthor;
	/**
     * 600K or 2.5M
     */
	private String size;
	private String version;
	/**
	 * such as :default.lwt or a.jpg
	 */
	private String pkg;
	public List<String> previewpath =new ArrayList<String>();
    public String attachment;
    private String id;
	public String thumbnailpath;
	
	/**
     * long length = file.length();
     */
	private Long length;
	
	private String createDate;
	/**
	 * 相对于在线主题中模块数量而言
	 */
    private String modelNum;
    /**
     * 相对于本地主题中模块数量而言
     */
    private ThemeModelInfo themeModelInfo;
    
    private boolean containLockScreen = false;
    
    /**
     * lwt源文件路径,如果lwtPath为空，则说明此lwt文件已经在/theme/lwt下,否则不是
     */
    private String lwtPath;
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ThemeBase(){
        
    }
	
	public ThemeBase(String cnName) {
		super();
		this.cnName = cnName;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if((o instanceof ThemeBase)&&o!=null&&this.getCnName()!=null){
			return this.getCnName().equals(((ThemeBase)o).cnName);
		}
		return super.equals(o);
	}
	public ThemeBase(ThemeModelInfo themeModelInfo,String pkg,String lwtPath,boolean parseJson){
		this.pkg = pkg;
		this.themeModelInfo = themeModelInfo;
		this.lwtPath = lwtPath;
		parseThemePkgInfo(parseJson);
	}

	public String getCnName() {
		return cnName;
	}

	public void setCnName(String cnName) {
		this.cnName = cnName;
	}

	public String getEnName() {
		return enName;
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public String getCnAuthor() {
		return cnAuthor;
	}

	public void setCnAuthor(String cnAuthor) {
		this.cnAuthor = cnAuthor;
	}

	public String getEnAuthor() {
		return enAuthor;
	}

	public void setEnAuthor(String enAuthor) {
		this.enAuthor = enAuthor;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPkg() {
		return pkg;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	/**
     * such as:default or a
     */
	public String getName() {
		
		return ThemeUtil.getNameNoBuffix(pkg);
	}

    public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getModelNum() {
		return modelNum;
	}

	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}

	public ThemeModelInfo getThemeModelInfo() {
		return themeModelInfo;
	}

	public void setThemeModelInfo(ThemeModelInfo themeModelInfo) {
		this.themeModelInfo = themeModelInfo;
	}

	public boolean getContainLockScreen() {
        return containLockScreen;
    }

    public void setContainLockScreen(boolean containLockScreen) {
        this.containLockScreen = containLockScreen;
    }

    public String getLwtPath() {
        return lwtPath;
    }

    public void setLwtPath(String lwtPath) {
        this.lwtPath = lwtPath;
    }

    private void parseThemePkgInfo(boolean parseJson) {
        if(!parseJson){
        	return;
        }
        try {
            
            if(lwtPath != null){
                File lwt = new File(lwtPath);
                
                this.setLength(lwt.length());
                this.setSize(ThemeUtil.fileLengthToSize(getLength()));
            }
            
            String nameNoLwt = null;
            if(ThemeConstants.LEWA.equals(pkg)){
                nameNoLwt = pkg;
            }else{
                nameNoLwt = ThemeUtil.getNameNoBuffix(pkg);
            }
             
            String themeInfoCN = new StringBuilder().append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/").append(nameNoLwt).append("/theme.json.zh_CN").toString();
            File cn_file = new File(themeInfoCN);
            if(cn_file.exists()){
            	String jsonStrCN = FileUtils.readFileToString(cn_file,"GBK");
                JSONObject cn = new JSONObject(jsonStrCN);
                
                setCnName(cn.getString("themename"));
                setCnAuthor(cn.getString("author"));
                setVersion(cn.getString("version"));
            }
            
            
            String themeInfoEN = new StringBuilder().append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/").append(nameNoLwt).append("/theme.json").toString();
            File en_file = new File(themeInfoEN);
            if(en_file.exists()){
            	String jsonStrEN = FileUtils.readFileToString(en_file,"GBK");
                JSONObject en = new JSONObject(jsonStrEN);
                setEnName(en.getString("themename"));
                setEnAuthor(en.getString("author"));
                setVersion(en.getString("version"));
            }
            
            
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "{ cnName = " + cnName +
            ", enName = " + enName +
            ", cnAuthor = " + cnAuthor +
            ", enAuthor = " + enAuthor +
            ", size = " + size +
            ", version = " + version +
            ", pkg = " + pkg +
            ", length = " + length +
            ", createDate = " + createDate +
            ", modelNum = " + modelNum +
            ", lwtPath = " + lwtPath +
            " }";
    }
    
    
	
}
