# Docker
variable "image" {}
variable "image_pull_policy" {
  default = "IfNotPresent"
}

 # Kubernetes
variable "namespace" {}
variable "enabled" {}
variable "replicas" {}
variable "cpu_limit" {}
variable "cpu_request" {}
variable "memory_limit" {}
variable "memory_request" {}
variable "node_selector_label" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}

 # Environment
variable "jvm_memory_limit" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "graphite_prefix" {
  default = ""
}
variable "env_vars" {}

 # App
variable "kafka_endpoint" {}
variable "metric_source_graphite_host" {}
variable "aggregator_producer_topic" {}
variable "metric_functions_input_file" {}
variable "is_graphite_server_metrictank" {}
variable "initContainer_image" {}
variable "download_input_file_command" {}
