package org.structr.web.entity.dom.relationship;

import org.structr.core.entity.relationship.AbstractChildren;
import org.structr.web.entity.dom.DOMNode;

/**
 *
 * @author Christian Morgner
 */
public class DOMChildren extends AbstractChildren<DOMNode, DOMNode> {

	@Override
	public Class<DOMNode> getSourceType() {
		return DOMNode.class;
	}

	@Override
	public Class<DOMNode> getTargetType() {
		return DOMNode.class;
	}
}