/*******************************************************************************
 * Copyright (c) 2017, Jeeeyul Lee.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeeeyul Lee - initial API and implementation and/or initial documentation
 * See https://github.com/jeeeyul/swtend.
 *******************************************************************************/
package org.docdriven.script.ui.snapshot;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class SWTExtensions {
	
	private static Integer MENU_BAR_HEIGHT = null;
	
	public static Rectangle setLocation(Rectangle rectangle, Point location) {
		rectangle.x = location.x;
		rectangle.y = location.y;
		return rectangle;
	}
	
	public static Point getSize(Rectangle rect) {
		return new Point(rect.width, rect.height);
	}
	
	public static Rectangle setSize(Rectangle rectangle, Point size) {
		rectangle.width = size.x;
		rectangle.height = size.y;
		return rectangle;
	}
	
	public static Point getLocation(Rectangle rectangle) {
		return new Point(rectangle.x, rectangle.y);
	}
	
	public static int getMenubarHeight() {
		if (MENU_BAR_HEIGHT != null) {
			return MENU_BAR_HEIGHT;
		}

		if (Display.getCurrent() == null) {
			throw new SWTException("Invalid Thread Exception");
		}

		Shell dummy = new Shell();
		Menu menu = new Menu(dummy, SWT.BAR);
		dummy.setMenuBar(menu);
		Rectangle boundsWithMenu = dummy.computeTrim(0, 0, 0, 0);

		dummy.setMenuBar(null);
		Rectangle boundsWithoutMenu = dummy.computeTrim(0, 0, 0, 0);

		dummy.dispose();

		MENU_BAR_HEIGHT = boundsWithMenu.height - boundsWithoutMenu.height;

		return MENU_BAR_HEIGHT;
	}
	
	public static Rectangle translate(Rectangle rectangle, int dx, int dy) {
		rectangle.x += dx;
		rectangle.y += dy;
		return rectangle;
	}
	
	public static Rectangle resize(Rectangle rectangle, int width, int height) {
		rectangle.width += width;
		rectangle.height += height;
		return rectangle;
	}
	
	public static <T extends Resource> void safeDispose(Collection<T> resource) {
		for (T r : resource) {
			safeDispose(r);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Resource> void safeDispose(T... resource) {
		for (Resource r : resource) {
			safeDispose(r);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <W extends Widget> void safeDispose(W... widgets) {
		for (Widget w : widgets) {
			safeDispose(w);
		}
	}
	
	public static <W extends Widget> void safeDispose(W widget) {
		if (widget != null && !widget.isDisposed()) {
			widget.dispose();
		}
	}

	public static <T extends Resource> void safeDispose(T r) {
		if (r != null && !r.isDisposed()) {
			r.dispose();
		}
	}
	
	public static Rectangle getExpanded(Rectangle rectangle, int amount) {
		return expand(getCopy(rectangle), amount);
	}
	
	public static Point getCopy(Point point) {
		return new Point(point.x, point.y);
	}

	public static Rectangle getCopy(Rectangle rectangle) {
		return new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	public static Rectangle expand(Rectangle rectangle, int amount) {
		return expand(rectangle, amount, amount, amount, amount);
	}
	
	public static Rectangle expand(Rectangle rectangle, int left, int top, int right, int bottom) {
		rectangle.x -= left;
		rectangle.y -= top;
		rectangle.width += left + right;
		rectangle.height += top + bottom;
		return rectangle;
	}
	
	public static Rectangle getShrinked(Rectangle rectangle, int amount) {
		return shrink(getCopy(rectangle), amount);
	}
	
	public static Rectangle shrink(Rectangle rectangle, int amount) {
		return expand(rectangle, -amount);
	}
	
	public static Rectangle relocateTopRightWith(Rectangle me, Point topRight) {
		me.x = topRight.x - me.width;
		me.y = topRight.y;
		return me;
	}

	public static Rectangle relocateTopRightWith(Rectangle me, Rectangle offset) {
		return relocateTopRightWith(me, getTopRight(offset));
	}
	
	public static Point getTopRight(Rectangle rectangle) {
		return new Point(rectangle.x + rectangle.width, rectangle.y);
	}

}
