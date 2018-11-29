package com.poshly.products.util

import com.poshly.core.zk.ZooKeeperClientConfiguration
import org.apache.curator.CuratorZookeeperClient
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.curator.framework.listen.Listenable
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.utils.EnsurePath
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.{Watcher, ZooKeeper}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

trait ZooKeeperClientMockComponent extends ZooKeeperClientConfiguration with MockitoSugar {
  override implicit val zkClient: CuratorFramework = mock[CuratorFramework]

  val _client = mock[CuratorZookeeperClient]
  val _backgroundPathable = mock[BackgroundPathable[java.util.List[String]]]
  val _ensurePath = mock[EnsurePath]
  val _getChildrenBuilder = mock[GetChildrenBuilder]
  val _setDataBuilder = mock[SetDataBuilder]
  val _deleteBuilder = mock[DeleteBuilder]
  val _stat = new Stat
  val _connectionStateListener = mock[Listenable[ConnectionStateListener]]
  val _zookeeper = mock[ZooKeeper]

  when(zkClient.getZookeeperClient).thenReturn(_client)
  when(zkClient.newNamespaceAwareEnsurePath(anyString())).thenReturn(_ensurePath)
  when(zkClient.setData()).thenReturn(_setDataBuilder)
  when(zkClient.getChildren()).thenReturn(_getChildrenBuilder)
  when(zkClient.delete()).thenReturn(_deleteBuilder)
  when(zkClient.getConnectionStateListenable()).thenReturn(_connectionStateListener)

  when(_getChildrenBuilder.usingWatcher(anyObject[Watcher]())).thenReturn(_backgroundPathable)
  when(_backgroundPathable.inBackground(anyObject[BackgroundCallback]())).thenReturn(_backgroundPathable)
  when(_setDataBuilder.forPath(anyString())).thenReturn(_stat)
  when(_setDataBuilder.forPath(anyString(), any[Array[Byte]]())).thenReturn(_stat)
  when(_client.getZooKeeper).thenReturn(_zookeeper)
  when(_zookeeper.exists(anyString(), anyBoolean())).thenReturn(_stat)

  val _deleteBuilderBackgroundVersionable = mock[BackgroundVersionable]
//  when(_deleteBuilderBackgroundVersionable.forPath(anyString())).thenReturn(_stat)
//  when(_deleteBuilderBackgroundVersionable.forPath(anyString(), any[Array[Byte]]())).thenReturn(_stat)
  when(_deleteBuilder.deletingChildrenIfNeeded()).thenReturn(_deleteBuilderBackgroundVersionable)
}
