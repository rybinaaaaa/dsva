   HOST=$1
   PORT=$2
   NODE_ID=$3

  # Отправляем GET-запрос на API для получения информации о ноде
  RESPONSE=$(curl -s "http://$HOST:1$PORT/$NODE_ID")

  # Проверка, вернул ли сервер успешный ответ
  if [ $? -eq 0 ]; then
    echo "Information retrieved from Node $NODE_ID:"
    echo "$RESPONSE"  # Выводим содержимое ответа от ноды
  else
    echo "Failed to retrieve information from Node $NODE_ID."
  fi