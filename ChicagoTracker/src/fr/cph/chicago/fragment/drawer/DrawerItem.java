package fr.cph.chicago.fragment.drawer;

public final class DrawerItem {

	private String name;
	private int imgId;

	public DrawerItem(final String name, final int imgId) {
		this.name = name;
		this.imgId = imgId;
	}

	public final String getName() {
		return name;
	}

	public final void setName(final String name) {
		this.name = name;
	}

	public final int getImgId() {
		return imgId;
	}

	public final void setImgId(final int imgId) {
		this.imgId = imgId;
	}
}
