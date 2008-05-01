package org.codehaus.cargo.container.deployable;

import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;

public class File extends AbstractDeployable{

	public File(String file) {
		super(file);
	}

	public DeployableType getType() {
		return DeployableType.FILE;
	}

}
