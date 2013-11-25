/**
 * Copyright 2013 Universidad Politécnica de Madrid - Center for Open Middleware (http://www.centeropenmiddleware.com)
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package es.com.upm.graphiti.adaptor.provider;

import java.util.Hashtable;
/**
 * Default Provider for the images in the Image Provider.
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class LocalImageProvider {

	private Hashtable<String, String> htKeyImage = new Hashtable<String, String>();

	private String providerId;

    // The prefix for all identifiers of this image provider

    protected static final String PREFIX = 
              "es.com.upm."; //$NON-NLS-1$

 

    // The image identifier for an Connection.

    public static final String IMG_TRANSITION_PAD = PREFIX + "transition.pad"; //$NON-NLS-1$
    public static final String IMG_SENDFILE_STATE = PREFIX + "sendfile.state"; //$NON-NLS-1$
    public static final String IMG_END_STATE = PREFIX + "end.state"; //$NON-NLS-1$
    public static final String IMG_START_STATE = PREFIX + "start.state"; //$NON-NLS-1$
    public static final String IMG_JOIN_STATE = PREFIX + "join.state"; //$NON-NLS-1$
    public static final String IMG_PAGE_STATE = PREFIX + "page.state"; //$NON-NLS-1$
    public static final String IMG_WHITE_PAGE_STATE = PREFIX + "whitePage.state"; //$NON-NLS-1$
    public static final String IMG_NAVIGATION_STATE = PREFIX + "navigation.state"; //$NON-NLS-1$
    public static final String IMG_EXT_NAVIGATION_STATE = PREFIX + "ext.navigation.state"; //$NON-NLS-1$
    public static final String IMG_TRANSITION = PREFIX + "transition"; //$NON-NLS-1$
    public static final String IMG_NAVIGATION_STATE_ICON =  PREFIX + "navigation.state.icon"; //$NON-NLS-1$
    public static final String IMG_PAGE_STATE_ICON =  PREFIX + "page.state.icon"; //$NON-NLS-1$
    public static final String IMG_END_STATE_ICON =  PREFIX + "end.state.icon"; //$NON-NLS-1$
    public static final String IMG_SENDFILE_STATE_ICON =  PREFIX + "send.state.icon"; //$NON-NLS-1$
    public static final String IMG_EXT_NAVIGATION_STATE_ICON =  PREFIX + "ext.navigation.state.icon"; //$NON-NLS-1$
    public static final String IMG_SUBNAVIGATION_END_ICON =  PREFIX + "end.subnavigation.state.icon"; //$NON-NLS-1$
    /**
     * Default Constructor.
     */
    public LocalImageProvider() {
		addAvailableImages();
	}
    /**
	 * Add image file path.
	 * 
	 * @param imageId
	 *            the image id
	 * @param imageFilePath
	 *            the image file path
	 */
	final protected void addImageFilePath(String imageId, String imageFilePath) {
		if (this.htKeyImage.get(imageId) != null) {
			System.out.println("Error, Imagen ya registrada");
		} else {
			this.htKeyImage.put(imageId, imageFilePath);
		}
	}
	/**
	 * Add all the defined FilePath to the provider  
	 */
    protected void addAvailableImages() 
    {
        addImageFilePath(IMG_PAGE_STATE_ICON, "icons/page.png"); //$NON-NLS-1$
        addImageFilePath(IMG_NAVIGATION_STATE_ICON, "icons/navigation_state_white.png"); //$NON-NLS-1$
        addImageFilePath(IMG_END_STATE_ICON, "icons/end.png"); //$NON-NLS-1$
        addImageFilePath(IMG_SENDFILE_STATE_ICON, "icons/sendfile.png"); //$NON-NLS-1$
        addImageFilePath(IMG_EXT_NAVIGATION_STATE_ICON, "icons/ext_navigation.png"); //$NON-NLS-1$

        addImageFilePath(IMG_TRANSITION_PAD, "icons/transition_pad.gif"); //$NON-NLS-1$
        addImageFilePath(IMG_SENDFILE_STATE, "icons/sendfile_state.png"); //$NON-NLS-1$
        addImageFilePath(IMG_END_STATE, "icons/end_state.png"); //$NON-NLS-1$
        addImageFilePath(IMG_START_STATE, "icons/start_state.gif"); //$NON-NLS-1$
        addImageFilePath(IMG_JOIN_STATE, "icons/join_state.gif"); //$NON-NLS-1$
        addImageFilePath(IMG_PAGE_STATE, "icons/page_state.png"); //$NON-NLS-1$
        addImageFilePath(IMG_WHITE_PAGE_STATE, "icons/white_page_state.png"); //$NON-NLS-1$
        addImageFilePath(IMG_NAVIGATION_STATE, "icons/navigation_state.png"); //$NON-NLS-1$
        addImageFilePath(IMG_EXT_NAVIGATION_STATE, "icons/ext_navigation_state.png"); //$NON-NLS-1$
        addImageFilePath(IMG_TRANSITION, "icons/transition.gif"); //$NON-NLS-1$
    }
    /**
     * Get FilePath to the imageId in the String
     * @param imageId - String with the imageId
     * @return the filePath to the imageId.
     */
    final public String getImageFilePath(String imageId) {
		Object htObject = this.htKeyImage.get(imageId);
		if (htObject instanceof String) {
			return (String) htObject;
		}
		return null;
	}
    /**
     * Get ProviderId
     * @return the providerId
     */
	public String getProviderId() {
		return providerId;
	}
	/**
	 * Set ProviderId
	 * @param providerId - String
	 */
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
}


