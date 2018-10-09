from django.shortcuts import render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
import base64
import time

# Create your views here.


@csrf_exempt
def process(request):
    if request.method == "POST":
        imgstring = request.POST['image']
        imgdata = base64.b64decode(imgstring)
        y = request.POST['demo']


        filename = 'some_image.jpg'
        with open(filename, 'wb') as f:
            f.write(imgdata)



            
        
    #print(x)

    print(y)

    time.sleep(5)

    # return the caption instead of the string below
    return HttpResponse('This is the generated caption')
 








