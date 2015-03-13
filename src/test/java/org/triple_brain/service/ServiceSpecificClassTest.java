/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package org.triple_brain.service;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.triple_brain.service.resources.vertex.VertexPublicAccessResource;
import org.triple_brain.service.vertex.VertexNonOwnedSurroundGraphResourceTest;
import org.triple_brain.service.vertex.VertexOwnedSurroundGraphResouceTest;
import org.triple_brain.service.vertex.VertexPublicAccessResourceTest;
import org.triple_brain.service.vertex.VertexResourceTest;


@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses({
        VertexOwnedSurroundGraphResouceTest.class
})
public class ServiceSpecificClassTest extends ServiceTestRunner{}