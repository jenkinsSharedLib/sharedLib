def user() {
  def props = readProperties  file:'${WORKSPACE}/user.properties'
  def Var1= props['a']
  echo "${Var1}"

}
