package org.vaadin.hene.popupbutton;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vaadin.hene.popupbutton.widgetset.client.ui.VPopupButton;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.tools.ReflectTools;
import com.vaadin.ui.Button;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;

/**
 * Server side component for the VPopupButton widget.
 */
// This class contains code from AbstractComponentContainer
@ClientWidget(VPopupButton.class)
public class PopupButton extends Button implements ComponentContainer {

	private static final long serialVersionUID = -3148268967211155218L;

	private static final Method COMPONENT_ATTACHED_METHOD = ReflectTools
			.findMethod(ComponentAttachListener.class,
					"componentAttachedToContainer", ComponentAttachEvent.class);

	private static final Method COMPONENT_DETACHED_METHOD = ReflectTools
			.findMethod(ComponentDetachListener.class,
					"componentDetachedFromContainer",
					ComponentDetachEvent.class);

	private static final Method POPUP_VISIBILITY_METHOD = ReflectTools
			.findMethod(PopupVisibilityListener.class, "popupVisibilityChange",
					PopupVisibilityEvent.class);

	private Component component;

	private boolean popupVisible = false;

	private int xOffset = 0;
	private int yOffset = 0;

	private boolean popupFixedPosition;

	private final List<String> styles = new ArrayList<String>();

	public PopupButton(String caption) {
		super(caption);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.Button#paintContent(com.vaadin.terminal.PaintTarget)
	 */
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addVariable(this, "popupVisible", popupVisible);
		if (popupVisible) {
			if (component == null) {
				throw new IllegalStateException(
						"component cannot be null. Use addComponent to set the component.");
			}
			target.startTag("component");
			component.paint(target);
			target.endTag("component");
		}
		if (popupFixedPosition) {
			target.addAttribute("position", "fixed");
		} else {
			target.addAttribute("position", "auto");
		}

		target.addAttribute("xoffset", xOffset);
		target.addAttribute("yoffset", yOffset);

		if (styles.size() > 0) {
			target.addAttribute("styles", styles.toArray());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.Button#changeVariables(java.lang.Object,
	 * java.util.Map)
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		if (variables.containsKey("popupVisible")) {
			setPopupVisible(((Boolean) variables.get("popupVisible"))
					.booleanValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.ui.ComponentContainer#addComponent(com.vaadin.ui.Component)
	 */
	public void addComponent(Component c) {
		component = c;
		for (String style : styles) {
			component.addStyleName(style);
		}
		requestRepaint();

		if (c instanceof ComponentContainer) {
			// Make sure we're not adding the component inside it's own content
			for (Component parent = this; parent != null; parent = parent
					.getParent()) {
				if (parent == c) {
					throw new IllegalArgumentException(
							"Component cannot be added inside it's own content");
				}
			}
		}

		if (c.getParent() != null) {
			// If the component already has a parent, try to remove it
			ComponentContainer oldParent = (ComponentContainer) c.getParent();
			oldParent.removeComponent(c);

		}

		c.setParent(this);
		fireEvent(new ComponentAttachEvent(this, component));
	}

	public void addListener(ComponentAttachListener listener) {
		addListener(ComponentContainer.ComponentAttachEvent.class, listener,
				COMPONENT_ATTACHED_METHOD);

	}

	public void addListener(ComponentDetachListener listener) {
		addListener(ComponentContainer.ComponentDetachEvent.class, listener,
				COMPONENT_DETACHED_METHOD);
	}

	@Override
	public void addStyleName(String style) {
		super.addStyleName(style);
		styles.add(style);
	}

	@Override
	public void removeStyleName(String style) {
		super.removeStyleName(style);
		styles.remove(style);
	}

	@Override
	public void setStyleName(String style) {
		super.setStyleName(style);
		styles.clear();
		styles.add(style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.ComponentContainer#getComponentIterator()
	 */
	public Iterator<Component> getComponentIterator() {
		return new Iterator<Component>() {

			private boolean first = component == null;

			public boolean hasNext() {
				return !first;
			}

			public Component next() {
				if (!first) {
					first = true;
					return component;
				} else {
					return null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Not supported in this implementation.
	 * 
	 * @see com.vaadin.ui.ComponentContainer#moveComponentsFrom(com.vaadin.ui.
	 *      ComponentContainer)
	 */
	public void moveComponentsFrom(ComponentContainer source) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.ComponentContainer#removeAllComponents()
	 */
	public void removeAllComponents() {
		if (component != null) {
			removeComponent(component);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.ui.ComponentContainer#removeComponent(com.vaadin.ui.Component)
	 */
	public void removeComponent(Component c) {
		if (c.getParent() == this) {
			c.setParent(null);
			fireEvent(new ComponentDetachEvent(this, c));
		}
		component = null;
		requestRepaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vaadin.ui.ComponentContainer#removeListener(com.vaadin.ui.
	 * ComponentContainer.ComponentAttachListener)
	 */
	public void removeListener(ComponentAttachListener listener) {
		removeListener(ComponentContainer.ComponentAttachEvent.class, listener,
				COMPONENT_ATTACHED_METHOD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vaadin.ui.ComponentContainer#removeListener(com.vaadin.ui.
	 * ComponentContainer.ComponentDetachListener)
	 */
	public void removeListener(ComponentDetachListener listener) {
		removeListener(ComponentContainer.ComponentDetachEvent.class, listener,
				COMPONENT_DETACHED_METHOD);
	}

	/**
	 * 
	 * Not supported in this implementation.
	 * 
	 * @see com.vaadin.ui.ComponentContainer#replaceComponent(com.vaadin.ui.Component,
	 *      com.vaadin.ui.Component)
	 */
	public void replaceComponent(Component oldComponent, Component newComponent) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.ComponentContainer#requestRepaintAll()
	 */
	public void requestRepaintAll() {
		requestRepaint();
		if (component != null) {
			if (component instanceof Form) {
				// Form has children in layout, but is not ComponentContainer
				component.requestRepaint();
				((Form) component).getLayout().requestRepaintAll();
			} else if (component instanceof Table) {
				((Table) component).requestRepaintAll();
			} else if (component instanceof ComponentContainer) {
				((ComponentContainer) component).requestRepaintAll();
			} else {
				component.requestRepaint();
			}
		}
	}

	/**
	 * Shows or hides popup.
	 * 
	 * @param popupVisible
	 *            if true, popup is set to visible, otherwise popup is hidden.
	 */
	public void setPopupVisible(boolean popupVisible) {
		if (this.popupVisible != popupVisible) {
			this.popupVisible = popupVisible;
			fireEvent(new PopupVisibilityEvent(this));
			requestRepaint();
		}
	}

	/**
	 * Checks if the popup is visible.
	 * 
	 * @return true, if popup is visible, false otherwise.
	 */
	public boolean isPopupVisible() {
		return popupVisible;
	}

	/**
	 * Set the content component of the popup.
	 * 
	 * @param component
	 *            the component to be displayed in the popup.
	 */
	public void setComponent(Component component) {
		addComponent(component);
	}

	/**
	 * Add a listener that is called whenever the visibility of the popup is
	 * changed.
	 * 
	 * @param listener
	 *            the listener to add
	 * @see PopupVisibilityListener
	 * @see PopupVisibilityEvent
	 * @see #removePopupVisibilityListener(PopupVisibilityListener)
	 * 
	 */
	public void addPopupVisibilityListener(PopupVisibilityListener listener) {
		addListener(PopupVisibilityEvent.class, listener,
				POPUP_VISIBILITY_METHOD);
	}

	/**
	 * Removes a previously added listener, so that it no longer receives events
	 * when the visibility of the popup changes.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @see PopupVisibilityListener
	 * @see #addPopupVisibilityListener(PopupVisibilityListener)
	 */
	public void removePopupVisibilityListener(PopupVisibilityListener listener) {
		removeListener(PopupVisibilityEvent.class, listener,
				POPUP_VISIBILITY_METHOD);
	}

	/**
	 * This event is received by the PopupVisibilityListeners when the
	 * visibility of the popup changes. You can get the new visibility directly
	 * with {@link #isPopupVisible()}, or get the PopupButton that produced the
	 * event with {@link #getPopupButton()}.
	 * 
	 */
	public class PopupVisibilityEvent extends Event {

		private static final long serialVersionUID = 3170716121022820317L;

		public PopupVisibilityEvent(PopupButton source) {
			super(source);
		}

		/**
		 * Get the PopupButton instance that is the source of this event.
		 * 
		 * @return the source PopupButton
		 */
		public PopupButton getPopupButton() {
			return (PopupButton) getSource();
		}

		/**
		 * Returns the current visibility of the popup.
		 * 
		 * @return true if the popup is visible
		 */
		public boolean isPopupVisible() {
			return getPopupButton().isPopupVisible();
		}
	}

	/**
	 * Defines a listener that can receive a PopupVisibilityEvent when the
	 * visibility of the popup changes.
	 * 
	 */
	public interface PopupVisibilityListener extends Serializable {
		/**
		 * Pass to {@link PopupButton#PopupVisibilityEvent} to start listening
		 * for popup visibility changes.
		 * 
		 * @param event
		 *            the event
		 * 
		 * @see {@link PopupVisibilityEvent}
		 * @see {@link PopupButton#addPopupVisibilityListener(PopupVisibilityListener)}
		 */
		public void popupVisibilityChange(PopupVisibilityEvent event);
	}

	/**
	 * 
	 * @return the amount of pixels that the popup is offset on the X-axis.
	 */
	public int getXOffset() {
		return xOffset;
	}

	/**
	 * Sets the amount of pixels that the popup should be offset on the X-axis.
	 * May be negative.
	 * 
	 * @param xOffset
	 *            X offset in pixels
	 */
	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	/**
	 * 
	 * @return the amount of pixels that the popup is offset on the Y-axis.
	 */
	public int getYOffset() {
		return yOffset;
	}

	/**
	 * Sets the amount of pixels that the popup should be offset on the X-axis.
	 * May be negative.
	 * 
	 * @param yOffset
	 *            Y offset in pixels
	 */
	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	/**
	 * Set the popup to have a fixed CSS position instead of the default
	 * Absolute.
	 * 
	 * @param popupFixedPosition
	 */
	public void setPopupFixedPosition(boolean popupFixedPosition) {
		this.popupFixedPosition = popupFixedPosition;
	}

	/**
	 * Does the popup use fixed positioning?
	 * 
	 * @return
	 */
	public boolean isPopupFixedPosition() {
		return popupFixedPosition;
	}

}