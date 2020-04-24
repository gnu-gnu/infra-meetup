# configure("2") indicates Vagrant version2
Vagrant.configure("2") do |config|
  # Virtual machine system
  config.vm.box = "centos/7"
  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 2
  end
  config.vm.synced_folder ".", "/vagrant", type: "virtualbox", disabled: true 
 
  ## machine specific settings
  # Virtual machine 1 - master node
  config.vm.define "vm0" do |cfg|
    cfg.vm.hostname = "gnu"
    cfg.vm.network "private_network", ip: "192.168.0.111"
    cfg.vm.network "forwarded_port", guest: 3000, host:3000
    cfg.vm.network "forwarded_port", guest: 8888, host:8888
    cfg.vm.network "forwarded_port", guest: 9090, host:9090
    cfg.vm.network "forwarded_port", guest: 9100, host:9100
    cfg.vm.network "forwarded_port", guest: 8080, host:8080
	cfg.vm.provider "virtualbox" do |vb|
		vb.name = "gnu"
	end
	cfg.vm.provision "shell", run: "always", inline: "yum -y install net-tools"
  end
 
  # common process
  config.vm.provision "shell", inline: <<-SHELL
  echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC6cxpBftMHkFkIeltX23Nh0CJPKHF3M73N65eLakPhd00LA6Gu3SUlORMRqVvp/lsEKjHLoi5X3LQGc8IQu/13K6Jwm+tWSNNVA/wfs+HiMzGOCCPsPdz+OAUTO7NU4jNRmmHlouNH5G52LhB8nLHg39Tkl/DMOr/MBywfDC7g1gbZn+h0dPBcSrASeDlZsamgfYtitNkW99xpM2AnfH9uReQj78VKegojRajx56jHA1ouGFT1FAiHuq1snirTKejY17szNiyEKKonAkjHFKe87gMTLsm2VVDelecW6PUX30FnSMuFg2SbET/CEbWpDCD6C3E7ytvcCdzwlNp6e8Ih imported-openssh-key" >> /home/vagrant/.ssh/authorized_keys
  mkdir -p /root/.ssh
  echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC6cxpBftMHkFkIeltX23Nh0CJPKHF3M73N65eLakPhd00LA6Gu3SUlORMRqVvp/lsEKjHLoi5X3LQGc8IQu/13K6Jwm+tWSNNVA/wfs+HiMzGOCCPsPdz+OAUTO7NU4jNRmmHlouNH5G52LhB8nLHg39Tkl/DMOr/MBywfDC7g1gbZn+h0dPBcSrASeDlZsamgfYtitNkW99xpM2AnfH9uReQj78VKegojRajx56jHA1ouGFT1FAiHuq1snirTKejY17szNiyEKKonAkjHFKe87gMTLsm2VVDelecW6PUX30FnSMuFg2SbET/CEbWpDCD6C3E7ytvcCdzwlNp6e8Ih imported-openssh-key" >> /root/.ssh/authorized_keys
  chmod 400 -R /root/.ssh
  echo "127.0.0.1 localhost" > /etc/hosts
  echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
  systemctl restart sshd
SHELL
end
